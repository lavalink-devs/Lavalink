package lavalink.server.io;

import net.dv8tion.jda.manager.ConnectionManager;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class ConnectionManagerImpl implements ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManagerImpl.class);
    private final WebSocket socket;


    ConnectionManagerImpl(WebSocket socket) {
        this.socket = socket;
    }

    @Override
    public void removeAudioConnection(String guildId) {
    }

    @Override
    public void queueAudioConnect(String guildId, String channelId) {
        log.warn("queueAudioConnect was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId);
    }

    @Override
    public void onDisconnect(String guildId) {
        JSONObject obj = new JSONObject()
                .put("op", "disconnected")
                .put("guildId", guildId);
        socket.send(obj.toString());
    }

}
