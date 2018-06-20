package lavalink.server.plugin;

import lavalink.server.io.SocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a lavalink plugin. To get properly registered and called,
 * a plugin must not only implement this class, but also have the
 * {@link AutoRegister AutoRegister} annotation.
 *
 * <br>Removing one of these will result in the plugin <b>not being loaded</b>.
 *
 * @implNote With the current ASM based implementation, CLASS retention
 * would be enough for the annotations defined in this class, but if
 * it's ever changed to a reflection this could possibly break. As such,
 * they're marked with RUNTIME retention.
 */
public interface LavalinkPlugin {
    /**
     * Marks a plugin to be automatically registered by lavalink. Currently,
     * it is the only possible way to load a plugin, but in the future other
     * loading methods could be added.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AutoRegister {}

    /**
     * Calls this plugin from separate threads, on a dedicated executor.
     *
     * Adding this annotation to classes that do not represent plugins has no effect.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Async {
        /**
         * Returns the core pool size wanted for this plugin's executor.
         *
         * @return Number of threads the executor should have.
         */
        int corePoolSize() default 1;
    }

    /**
     * Called when lavalink is done starting.
     *
     * @param server SocketServer instance used by lavalink.
     */
    default void onStart(SocketServer server) {}

    /**
     * Called when the websocket handshake is received. Allows setting, for example, custom headers.
     *
     * @param conn Websocket connection.
     * @param draft Draft being used.
     * @param request Client handshake received.
     */
    default void onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) {}

    /**
     * Called when a websocket connection is opened.
     *
     * @param webSocket Websocket connection.
     * @param clientHandshake Client handshake received.
     */
    default void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {}

    /**
     * Called when a websocket is closed. Sending messages to this socket is <b>NOT</b> safe.
     *
     * @param webSocket Socket closed.
     * @param code Close code.
     * @param reason Close reason, if any.
     */
    default void onClose(WebSocket webSocket, int code, String reason) {}

    /**
     * Called before lavalink shuts down. Use this method to release any resources
     * you might have acquired.
     */
    default void onShutdown() {}
}
