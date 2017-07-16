package lavalink.server.io;

import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreClientImpl implements net.dv8tion.jda.CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreClientImpl.class);

    private final WebSocket socket;

    public CoreClientImpl(WebSocket socket) {
        this.socket = socket;
    }

    @Override
    public void sendWS(String s) {
        log.info("SEND_WS " + s);
    }

    @Override
    public boolean isConnected() {
        return false;
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
