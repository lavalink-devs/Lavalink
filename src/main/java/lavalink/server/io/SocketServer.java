package lavalink.server.io;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private final String password;

    public SocketServer(String password) {
        this.password = password;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        if (clientHandshake.getFieldValue("Authorization").equals(password)) {
            log.info("Connection opened from " + webSocket.getRemoteSocketAddress().toString() + " with protocol " + webSocket.getDraft());
        } else {
            log.error("Authentication failed from " + webSocket.getRemoteSocketAddress().toString() + " with protocol " + webSocket.getDraft());
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info("Connection closed from " + webSocket.getRemoteSocketAddress().toString() + " with protocol " + webSocket.getDraft());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JSONObject json = new JSONObject(s);

        switch (json.getString("op")) {
            case "create":
                break;
            case "destroy":
                break;
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log.error("Caught exception in websocket", e);
    }

    @Override
    public void onStart() {
        log.info("Started WS server");
    }
}
