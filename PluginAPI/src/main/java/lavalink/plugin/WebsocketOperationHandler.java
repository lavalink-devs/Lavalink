package lavalink.plugin;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

/**
 * Handles a websocket operation.
 */
public interface WebsocketOperationHandler {
    /**
     * Called when an operation this handler is registered for is received.
     *
     * @param server SocketServer used by lavalink.
     * @param context Context for the peer websocket.
     * @param webSocket WebSocket for the peer.
     * @param json Payload received.
     */
    void handle(ISocketServer server, ISocketContext context, WebSocket webSocket, JSONObject json);
}
