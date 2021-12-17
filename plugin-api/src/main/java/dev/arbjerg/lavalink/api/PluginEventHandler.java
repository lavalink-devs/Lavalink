package dev.arbjerg.lavalink.api;

/**
 * Must be provided as a bean
 */
public interface PluginEventHandler {

    /**
     * Fired upon a new WebSocket being opened
     * @param context the websocket
     * @param resumed if the context was resumed and thus reused
     */
    default void onWebSocketOpen(ISocketContext context, boolean resumed) {}

    /**
     * Fired upon a WebSocket being closed
     * @param context the websocket
     */
    default void onWebSocketClose(ISocketContext context) {}

    /**
     * Fired upon a WebSocket message being received
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    default void onWebsocketMessageIn(ISocketContext context, String message) {}

    /**
     * Fired upon a WebSocket message being sent
     * @param context the websocket
     * @param message the message, presumably in JSON
     */
    default void onWebSocketMessageOut(ISocketContext context, String message) {}

    /**
     * Fired upon a new player being created
     * @param context the websocket
     * @param player the new player
     */
    default void onNewPlayer(ISocketContext context, IPlayer player) {}
}
