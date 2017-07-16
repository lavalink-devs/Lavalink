package lavalink.server.io;

import net.dv8tion.jda.CoreClient;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreClientImpl implements CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreClientImpl.class);

    private final WebSocket socket;
    private int shardId;

    CoreClientImpl(WebSocket socket, int shardId) {
        this.socket = socket;
        this.shardId = shardId;
    }

    @Override
    public void sendWS(String message) {
        log.info(message);
        JSONObject json = new JSONObject();
        json.put("op", "sendWS");
        json.put("shardId", shardId);
        json.put("message", message);
        socket.send(json.toString());
    }

    //TODO: Implement these

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean inGuild(String s) {
        return true;
    }

    @Override
    public boolean voiceChannelExists(String s) {
        return true;
    }

    @Override
    public boolean hasPermissionInChannel(String s, long l) {
        return true;
    }

}
