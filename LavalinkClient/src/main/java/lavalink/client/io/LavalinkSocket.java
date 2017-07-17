package lavalink.client.io;

import lavalink.client.LavalinkUtil;
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

import java.net.URI;
import java.util.Map;

public class LavalinkSocket extends WebSocketClient {

    private static final Logger log = LoggerFactory.getLogger(LavalinkSocket.class);

    private static final int TIMEOUT_MS = 5000;
    private final Lavalink lavalink;

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
    public void onOpen(ServerHandshake handshakedata) {
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
            default:
                log.warn("Unexpected operation: " + json.getString("op"));
                break;
        }
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
