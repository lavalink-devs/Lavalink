package lavalink.client.io;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
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
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
