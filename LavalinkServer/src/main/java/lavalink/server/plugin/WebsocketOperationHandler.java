package lavalink.server.plugin;

import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

public interface WebsocketOperationHandler {
    void handle(SocketServer server, SocketContext context, WebSocket webSocket, JSONObject json);
}
