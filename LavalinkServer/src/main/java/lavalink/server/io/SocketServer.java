/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.io;

import com.github.shredder121.asyncaudio.jda.AsyncPacketProviderFactory;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import lavalink.server.config.AudioSendFactoryConfiguration;
import lavalink.server.config.ServerConfig;
import lavalink.server.player.Player;
import lavalink.server.player.TrackEndMarkerHandler;
import lavalink.server.util.Util;
import lavalink.server.util.Ws;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import space.npstr.magma.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class SocketServer extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    private final MagmaApi magmaApi = MagmaApi.of(this::getAudioSendFactory);
    // userId <-> shardCount
    private final Map<String, Integer> shardCounts = new ConcurrentHashMap<>();
    private final Map<String, SocketContext> contextMap = new HashMap<>();
    private final ServerConfig serverConfig;
    private final Supplier<AudioPlayerManager> audioPlayerManagerSupplier;
    private final AudioSendFactoryConfiguration audioSendFactoryConfiguration;
    private final ConcurrentHashMap<Integer, IAudioSendFactory> sendFactories = new ConcurrentHashMap<>();

    public SocketServer(ServerConfig serverConfig, Supplier<AudioPlayerManager> audioPlayerManagerSupplier,
                        AudioSendFactoryConfiguration audioSendFactoryConfiguration) {
        this.serverConfig = serverConfig;
        this.audioPlayerManagerSupplier = audioPlayerManagerSupplier;
        this.audioSendFactoryConfiguration = audioSendFactoryConfiguration;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        int shardCount = Integer.parseInt(session.getHandshakeHeaders().getFirst("Num-Shards"));
        String userId = session.getHandshakeHeaders().getFirst("User-Id");

        shardCounts.put(userId, shardCount);

        contextMap.put(session.getId(), new SocketContext(audioPlayerManagerSupplier, session, this, userId, magmaApi));
        log.info("Connection successfully established from " + session.getRemoteAddress());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SocketContext context = contextMap.remove(session.getId());
        if (context != null) {
            log.info("Connection closed from {} -- {}", session.getRemoteAddress(), status);
            context.shutdown();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            handleTextMessageSafe(session, message);
        } catch (Exception e) {
            log.error("Exception while handling websocket message", e);
        }
    }

    private void handleTextMessageSafe(WebSocketSession session, TextMessage message) {
        JSONObject json = new JSONObject(message.getPayload());

        log.info(message.getPayload());

        if (!session.isOpen()) {
            log.error("Ignoring closing websocket: " + session.getRemoteAddress());
            return;
        }

        switch (json.getString("op")) {
            /* JDAA ops */
            case "voiceUpdate":
                String sessionId = json.getString("sessionId");
                String guildId = json.getString("guildId");

                JSONObject event = json.getJSONObject("event");
                String endpoint = event.optString("endpoint");
                String token = event.getString("token");

                //discord sometimes send a partial server update missing the endpoint, which can be ignored.
                if (endpoint == null || endpoint.isEmpty()) {
                    return;
                }

                SocketContext sktContext = contextMap.get(session.getId());
                Member member = MagmaMember.builder()
                        .userId(sktContext.getUserId())
                        .guildId(guildId)
                        .build();
                ServerUpdate serverUpdate = MagmaServerUpdate.builder()
                        .sessionId(sessionId)
                        .endpoint(endpoint)
                        .token(token)
                        .build();
                magmaApi.provideVoiceServerUpdate(member, serverUpdate);
                break;

            /* Player ops */
            case "play":
                try {
                    SocketContext ctx = contextMap.get(session.getId());
                    Player player = ctx.getPlayer(json.getString("guildId"));
                    AudioTrack track = Util.toAudioTrack(ctx.getAudioPlayerManager(), json.getString("track"));
                    if (json.has("startTime")) {
                        track.setPosition(json.getLong("startTime"));
                    }
                    if (json.has("endTime")) {
                        track.setMarker(new TrackMarker(json.getLong("endTime"), new TrackEndMarkerHandler(player)));
                    }

                    player.setPause(json.optBoolean("pause", false));
                    if (json.has("volume")) {
                        player.setVolume(json.getInt("volume"));
                    }

                    player.play(track);

                    SocketContext context = contextMap.get(session.getId());

                    Member m = MagmaMember.builder()
                            .userId(context.getUserId())
                            .guildId(json.getString("guildId"))
                            .build();
                    magmaApi.setSendHandler(m, context.getPlayer(json.getString("guildId")));

                    sendPlayerUpdate(session, player);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "stop":
                Player player = contextMap.get(session.getId()).getPlayer(json.getString("guildId"));
                player.stop();
                break;
            case "pause":
                Player player2 = contextMap.get(session.getId()).getPlayer(json.getString("guildId"));
                player2.setPause(json.getBoolean("pause"));
                sendPlayerUpdate(session, player2);
                break;
            case "seek":
                Player player3 = contextMap.get(session.getId()).getPlayer(json.getString("guildId"));
                player3.seekTo(json.getLong("position"));
                sendPlayerUpdate(session, player3);
                break;
            case "volume":
                Player player4 = contextMap.get(session.getId()).getPlayer(json.getString("guildId"));
                player4.setVolume(json.getInt("volume"));
                break;
            case "destroy":
                SocketContext socketContext = contextMap.get(session.getId());
                Player player5 = socketContext.getPlayers().remove(json.getString("guildId"));
                if (player5 != null) player5.stop();
                Member mem = MagmaMember.builder()
                        .userId(socketContext.getUserId())
                        .guildId(json.getString("guildId"))
                        .build();
                magmaApi.removeSendHandler(mem);
                magmaApi.closeConnection(mem);
                break;
            default:
                log.warn("Unexpected operation: " + json.getString("op"));
                break;
        }
    }

    public static void sendPlayerUpdate(WebSocketSession session, Player player) {
        JSONObject json = new JSONObject();
        json.put("op", "playerUpdate");
        json.put("guildId", player.getGuildId());
        json.put("state", player.getState());

        Ws.send(session, json);
    }

    Collection<SocketContext> getContexts() {
        return contextMap.values();
    }

    private IAudioSendFactory getAudioSendFactory(Member member) {
        int shardCount = shardCounts.getOrDefault(member.getUserId(), 1);
        int shardId = Util.getShardFromSnowflake(member.getGuildId(), shardCount);

        return sendFactories.computeIfAbsent(shardId % audioSendFactoryConfiguration.getAudioSendFactoryCount(),
                integer -> {
                    Integer customBuffer = serverConfig.getBufferDurationMs();
                    NativeAudioSendFactory nativeAudioSendFactory;
                    if (customBuffer != null) {
                        nativeAudioSendFactory = new NativeAudioSendFactory(customBuffer);
                    } else {
                        nativeAudioSendFactory = new NativeAudioSendFactory();
                    }

                    return AsyncPacketProviderFactory.adapt(nativeAudioSendFactory);
                });
    }
}
