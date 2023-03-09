package dev.arbjerg.lavalink.api;

import kotlinx.serialization.SerializationStrategy;
import kotlinx.serialization.json.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * Represents a WebSocket connection
 */
public interface ISocketContext {

    /**
     * @return The session ID of the connection
     */
    @NonNull
    String getSessionId();

    /**
     * @return the User ID of the Client.
     */
    long getUserId();

    /**
     * @return the name of the Client, or null if it was not specified.
     */
    @Nullable
    String getClientName();

    /**
     * Returns the player of a guild. Never returns null.
     *
     * @param guildId the guild the player is associated with
     * @return a potentially newly-created player
     */
    IPlayer getPlayer(long guildId);

    /**
     * @return a read-only map of all players associated by their guild
     */
    Map<Long, IPlayer> getPlayers();

    /**
     * @param guildId guild for which to remove player state from
     */
    void destroyPlayer(long guildId);

    /**
     * @param serializer a {@link SerializationStrategy} capable of serializing {@link  T}
     * @param message    a message to send to the WebSocket client, it should be compatible with Jackson.
     */
    <T> void sendMessage(@NotNull SerializationStrategy<T> serializer, T message);

    /**
     * @param message    a message to send to the WebSocket client, it should be compatible with Jackson.
     */
    default void sendMessage(JsonElement message) {
        sendMessage(JsonElement.Companion.serializer(), message);
    }

    /**
     * @return the state of the context
     */
    State getState();

    /**
     * Closes this WebSocket
     */
    void closeWebSocket();

    /**
     * @param closeCode the close code to send to the client
     */
    void closeWebSocket(int closeCode);

    /**
     * @param closeCode the close code to send to the client
     * @param reason    the close reason to send to the client
     */
    void closeWebSocket(int closeCode, String reason);

    enum State {
        /**
         * The context has an open WebSocket
         */
        OPEN,
        /**
         * The context does not have an open WebSocket, but can later be resumed
         */
        RESUMABLE,

        /**
         * The WebSocket has closed and this context will never be used again
         */
        DESTROYED
    }
}
