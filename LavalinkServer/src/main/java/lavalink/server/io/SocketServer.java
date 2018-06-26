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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import lavalink.server.config.AudioSendFactoryConfiguration;
import lavalink.server.config.ServerConfig;
import lavalink.server.config.WebsocketConfig;
import lavalink.server.player.Player;
import lavalink.server.player.TrackEndMarkerHandler;
import lavalink.server.plugin.WebsocketOperationHandler;
import lavalink.server.plugin.loader.PluginManager;
import lavalink.server.util.Util;
import net.dv8tion.jda.Core;
import net.dv8tion.jda.manager.AudioManager;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static lavalink.server.io.WSCodes.AUTHORIZATION_REJECTED;
import static lavalink.server.io.WSCodes.INTERNAL_ERROR;

@Component
public class SocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    private static final Map<String, WebsocketOperationHandler> DEFAULT_HANDLERS;

    private final Map<WebSocket, SocketContext> contextMap = new HashMap<>();
    private final ServerConfig serverConfig;
    private final Supplier<AudioPlayerManager> audioPlayerManagerSupplier;
    private final AudioSendFactoryConfiguration audioSendFactoryConfiguration;
    private final PluginManager pluginManager;
    private final Map<String, WebsocketOperationHandler> handlers;

    static {
        Map<String, WebsocketOperationHandler> map = new HashMap<>();
        map.put("voiceUpdate", (server, context, webSocket, json) -> {
            Core core = context.getCore(server.getShardId(webSocket, json));
            core.provideVoiceServerUpdate(
                    json.getString("sessionId"),
                    json.getJSONObject("event")
            );
            core.getAudioManager(json.getJSONObject("event").getString("guild_id")).setAutoReconnect(false);
        });
        map.put("play", (server, context, webSocket, json) -> {
            try {
                Player player = context.getPlayer(json.getString("guildId"));
                AudioTrack track = Util.toAudioTrack(context.getAudioPlayerManager(), json.getString("track"));
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

                context.getCore(server.getShardId(webSocket, json)).getAudioManager(json.getString("guildId"))
                        .setSendingHandler(context.getPlayer(json.getString("guildId")));
                sendPlayerUpdate(webSocket, player);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        map.put("stop", (server, context, webSocket, json) -> {
            Player player = context.getPlayer(json.getString("guildId"));
            player.stop();
        });
        map.put("pause", (server, context, webSocket, json) -> {
            Player player = context.getPlayer(json.getString("guildId"));
            player.setPause(json.getBoolean("pause"));
            sendPlayerUpdate(webSocket, player);
        });
        map.put("seek", (server, context, webSocket, json) -> {
            Player player = context.getPlayer(json.getString("guildId"));
            player.seekTo(json.getLong("position"));
            sendPlayerUpdate(webSocket, player);
        });
        map.put("volume", (server, context, webSocket, json) -> {
            Player player = context.getPlayer(json.getString("guildId"));
            player.setVolume(json.getInt("volume"));
            sendPlayerUpdate(webSocket, player);
        });
        map.put("destroy", (server, context, webSocket, json) -> {
            Player player = context.getPlayers().remove(json.getString("guildId"));
            if (player != null) player.stop();
            AudioManager audioManager = context
                    .getCore(server.getShardId(webSocket, json))
                    .getAudioManager(json.getString("guildId"));
            audioManager.setSendingHandler(null);
            audioManager.closeAudioConnection();
        });
        DEFAULT_HANDLERS = Collections.unmodifiableMap(map);
    }

    public SocketServer(WebsocketConfig websocketConfig, ServerConfig serverConfig, Supplier<AudioPlayerManager> audioPlayerManagerSupplier,
                        AudioSendFactoryConfiguration audioSendFactoryConfiguration, PluginManager pluginManager) {
        super(new InetSocketAddress(websocketConfig.getHost(), websocketConfig.getPort()));
        this.setReuseAddr(true);
        this.serverConfig = serverConfig;
        this.audioPlayerManagerSupplier = audioPlayerManagerSupplier;
        this.audioSendFactoryConfiguration = audioSendFactoryConfiguration;
        this.pluginManager = pluginManager;
        this.handlers = new HashMap<>(DEFAULT_HANDLERS);
    }

    @Override
    @PostConstruct
    public void start() {
        super.start();
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        pluginManager.callOnWebsocketHandshakeReceivedAsServer(conn, draft, request);
        builder.put("Lavalink-Major-Version", "3");
        return builder;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            int shardCount = Integer.parseInt(clientHandshake.getFieldValue("Num-Shards"));
            String userId = clientHandshake.getFieldValue("User-Id");

            if (clientHandshake.getFieldValue("Authorization").equals(serverConfig.getPassword())) {
                log.info("Connection opened from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                contextMap.put(webSocket, new SocketContext(audioPlayerManagerSupplier, serverConfig, webSocket,
                        audioSendFactoryConfiguration, this, userId, shardCount));
            } else {
                log.error("Authentication failed from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                webSocket.close(AUTHORIZATION_REJECTED, "Authorization rejected");
            }
            pluginManager.callOnOpen(webSocket, clientHandshake);
        } catch (Exception e) {
            log.error("Error when opening websocket", e);
            webSocket.close(INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public void onCloseInitiated(WebSocket webSocket, int code, String reason) {
        close(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason, boolean remote) {
        close(webSocket, code, reason);
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        close(webSocket, code, reason);
    }

    // WebSocketServer has a very questionable attitude towards communicating close events, so we override ALL the closing methods
    private void close(WebSocket webSocket, int code, String reason) {
        SocketContext context = contextMap.remove(webSocket);
        if (context != null) {
            pluginManager.callOnClose(webSocket, code, reason);
            log.info("Connection closed from {} with protocol {} with reason {} with code {}",
                    webSocket.getRemoteSocketAddress().toString(), webSocket.getDraft(), reason, code);
            context.shutdown();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JSONObject json = new JSONObject(s);

        log.info(s);

        if (webSocket.isClosing()) {
            log.error("Ignoring closing websocket: " + webSocket.getRemoteSocketAddress().toString());
            return;
        }

        WebsocketOperationHandler handler = handlers.get(json.getString("op"));
        if(handler == null) {
            log.warn("Unexpected operation: " + json.getString("op"));
            return;
        }

        SocketContext context = contextMap.get(webSocket);

        handler.handle(this, context, webSocket, json);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log.error("Caught exception in websocket", e);
    }

    @Override
    public void onStart() {
        log.info("Started WS server with port " + getPort());
        if(pluginManager.hasPlugins()) {
            log.info("Initializing plugins");
            pluginManager.callOnStart(this);
        }
    }

    public static void sendPlayerUpdate(WebSocket webSocket, Player player) {
        JSONObject json = new JSONObject();
        json.put("op", "playerUpdate");
        json.put("guildId", player.getGuildId());
        json.put("state", player.getState());

        webSocket.send(json.toString());
    }

    //Shorthand method
    public int getShardId(WebSocket webSocket, JSONObject json) {
        return Util.getShardFromSnowflake(json.getString("guildId"), contextMap.get(webSocket).getShardCount());
    }

    public Collection<SocketContext> getContexts() {
        return contextMap.values();
    }

    public Map<String, WebsocketOperationHandler> getHandlers() {
        return handlers;
    }

    public WebsocketOperationHandler getHandler(String op) {
        return handlers.get(op);
    }

    public boolean registerHandler(String op, WebsocketOperationHandler handler, boolean override) {
        Objects.requireNonNull(op, "Op may not be null");
        Objects.requireNonNull(handler, "Handler may not be null");
        if(override) {
            handlers.put(op, handler);
            return true;
        }
        return handlers.putIfAbsent(op, handler) == null;
    }

    public boolean registerHandler(String op, WebsocketOperationHandler handler) {
        return registerHandler(op, handler, false);
    }

    public Supplier<AudioPlayerManager> getAudioPlayerManagerSupplier() {
        return audioPlayerManagerSupplier;
    }
}
