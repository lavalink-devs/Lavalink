package lavalink.server.io;

import net.dv8tion.jda.manager.ConnectionManager;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class ConnectionManagerImpl implements ConnectionManager {
    private final WebSocket socket;

    ConnectionManagerImpl(WebSocket socket) {
        this.socket = socket;
    }

    @Override
    public void removeAudioConnection(String guildId) {
    }

    @Override
    public void queueAudioConnect(String guildId, String channelId) {
    }

    @Override
    public void shouldReconnect(String guildId) {
        JSONObject obj = new JSONObject()
                .put("op", "disconnected")
                .put("guildId", guildId);
        socket.send(obj.toString());
    }


}
