package dev.arbjerg.lavalink.api

/**
 * Must be provided as a bean
 */
abstract class PluginEventHandler {
    /**
     * Fired upon a new WebSocket being opened
     *
     * @param context the new websocket
     * @param resumed if the context was resumed and thus reused
     */
    open fun onWebSocketOpen(context: ISocketContext, resumed: Boolean) = Unit

    /**
     * Fired upon a WebSocket being closed while being configured for resuming
     *
     * @param context the socket context
     */
    open fun onSocketContextPaused(context: ISocketContext) = Unit

    /**
     * Fired once the WebSocket is closed without being resumable or when a WebSocket can no longer be resumed
     *
     * @param context the socket context
     */
    open fun onSocketContextDestroyed(context: ISocketContext) = Unit

    /**
     * Fired upon a WebSocket message being received
     *
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Usage of websocket commands is deprecated, use REST API instead")
    open fun onWebsocketMessageIn(context: ISocketContext, message: String) = Unit

    /**
     * Fired upon a WebSocket message being sent
     *
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    open fun onWebSocketMessageOut(context: ISocketContext, message: String) = Unit

    /**
     * Fired upon a new player being created
     *
     * @param context the websocket
     * @param player  the new player
     */
    open fun onNewPlayer(context: ISocketContext, player: IPlayer) = Unit

    /**
     * Fired upon a player being destroyed
     *
     * @param context the websocket
     * @param player  the player to be destroyed
     */
    open fun onDestroyPlayer(context: ISocketContext, player: IPlayer) = Unit
}
