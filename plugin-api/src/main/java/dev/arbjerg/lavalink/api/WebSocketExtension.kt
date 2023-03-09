package dev.arbjerg.lavalink.api

import org.json.JSONObject

/**
 * When added as a bean, adds an operation to the WebSocket API that clients can invoke.
 */
@Deprecated("Superseded", ReplaceWith("ISocketContext"))
interface WebSocketExtension {
    /**
     * @return the "op" value to be sent by clients
     */
    val opName: String

    /**
     * Hook for receiving messages
     * @param context the WebSocket
     * @param message the entire JSON message received
     */
    fun onInvocation(context: ISocketContext?, message: JSONObject?)
}
