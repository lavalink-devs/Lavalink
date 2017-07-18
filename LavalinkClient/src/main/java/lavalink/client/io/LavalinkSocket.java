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

package lavalink.client.io;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.LavalinkUtil;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.TrackEndEvent;
import lavalink.client.player.event.TrackExceptionEvent;
import lavalink.client.player.event.TrackStuckEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class LavalinkSocket extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LavalinkSocket.class);

    private static final int TIMEOUT_MS = 5000;
    private final Lavalink lavalink;
    RemoteStats stats;

    LavalinkSocket(Lavalink lavalink, URI serverUri, Draft protocolDraft, Map<String, String> headers) {
        super(serverUri, protocolDraft, headers, TIMEOUT_MS);
        this.lavalink = lavalink;
        try {
            this.connectBlocking();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("Received handshake from server");
    }

    @Override
    public void onMessage(String message) {
        JSONObject json = new JSONObject(message);

        log.info(message);

        switch (json.getString("op")) {
            case "sendWS":
                JDAImpl jda = (JDAImpl) lavalink.getShard(json.getInt("shardId"));
                jda.getClient().send(json.getString("message"));
                break;
            case "validationReq":
                int sId = LavalinkUtil.getShardFromSnowflake(json.getString("guildOrChannelId"), lavalink.getNumShards());
                JDA jda2 = lavalink.getShard(sId);
                // Check if the VC or Guild exists, and that we have access to the VC

                JSONObject res = new JSONObject();
                res.put("op", "validationRes");
                String mysteryId = json.getString("guildOrChannelId");
                Guild guild = jda2.getGuildById(mysteryId);
                VoiceChannel vc = jda2.getVoiceChannelById(mysteryId);

                if (vc != null) {
                    guild = vc.getGuild();
                    res.put("guildId", guild.getId());
                    res.put("channelId", vc.getId());
                    res.put("valid", PermissionUtil.checkPermission(vc, guild.getSelfMember(),
                            Permission.VOICE_CONNECT, Permission.VOICE_SPEAK));
                    send(res.toString());
                    break;
                }

                if (guild == null) {
                    res.put("guildId", mysteryId);
                    res.put("channelId", mysteryId);
                    res.put("valid", false);
                    send(res.toString());
                    break;
                }

                res.put("guildId", mysteryId);
                res.put("valid", true);
                send(res.toString());
                break;
            case "isConnectedReq":
                JDAImpl jda3 = (JDAImpl) lavalink.getShard(json.getInt("shardId"));
                JSONObject res2 = new JSONObject();
                res2.put("op", "isConnectedRes");
                res2.put("shardId", json.getInt("shardId"));
                res2.put("connected", jda3.getClient().isConnected());
                send(res2.toString());
                break;
            case "playerUpdate":
                ((LavalinkPlayer) lavalink.getPlayer(json.getString("guildId")))
                        .provideState(json.getJSONObject("state"));
                break;
            case "stats":
                stats = new RemoteStats(json);
                break;
            case "event":
                try {
                    handleEvent(json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                log.warn("Unexpected operation: " + json.getString("op"));
                break;
        }
    }

    /**
     * Implementation details:
     * The only events extending {@link lavalink.client.player.event.PlayerEvent} produced by the remote server are these:
     * 1. TrackEndEvent
     * 2. TrackExceptionEvent
     * 3. TrackStuckEvent
     * <p>
     * The remaining are caused by the client
     */
    private void handleEvent(JSONObject json) throws IOException {
        LavalinkPlayer player = (LavalinkPlayer) lavalink.getPlayer(json.getString("guildId"));
        PlayerEvent event = null;

        switch (json.getString("type")) {
            case "TrackEndEvent":
                event = new TrackEndEvent(player,
                        LavalinkUtil.toAudioTrack(json.getString("track")),
                        AudioTrackEndReason.valueOf(json.getString("reason"))
                );
                break;
            case "TrackExceptionEvent":
                event = new TrackExceptionEvent(player,
                        LavalinkUtil.toAudioTrack(json.getString("track")),
                        new RemoteTrackException(json.getString("error"))
                );
                break;
            case "TrackStuckEvent":
                event = new TrackStuckEvent(player,
                        LavalinkUtil.toAudioTrack(json.getString("track")),
                        json.getLong("thresholdMs")
                );
                break;
            default:
                log.warn("Unexpected event type: " + json.getString("type"));
                break;
        }

        if (event != null) player.emitEvent(event);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection closed with reason " + code + ": " + reason + " :: Remote=" + remote);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Caught exception in websocket", ex);
    }
}
