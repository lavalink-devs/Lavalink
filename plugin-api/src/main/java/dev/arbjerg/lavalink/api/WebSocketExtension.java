package dev.arbjerg.lavalink.api;

import org.json.JSONObject;

/**
 * When added as a bean, adds an operation to the WebSocket API that clients can invoke.
 */
public interface WebSocketExtension {
    String getOpName();
    void onInvocation(ISocketContext context, JSONObject message);
}
