package dev.arbjerg.lavalink.api

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement

/**
 * Represents a WebSocket connection
 */
interface ISocketContext {
    val sessionId: String

    /**
     * @return the User ID of the Client.
     */
    val userId: Long

    val clientName: String?

    /**
     * Returns the player of a guild. Never returns null.
     *
     * @param guildId the guild the player is associated with
     * @return a potentially newly-created player
     */
    fun getPlayer(guildId: Long): IPlayer

    /**
     * @return a read-only map of all players associated by their guild
     */
    val players: Map<Long, IPlayer>

    /**
     * @param guildId guild for which to remove player state from
     */
    fun destroyPlayer(guildId: Long)

    /**
     * @param serializer a [SerializationStrategy] capable of serializing [T]
     * @param message    a message to send to the WebSocket client, it should be compatible with kotlinx.serialization.
     */
    fun <T> sendMessage(serializer: SerializationStrategy<T>, message: T)

    /**
     * @param message    a message to send to the WebSocket client, it should be compatible with Jackson.
     */
    fun sendMessage(message: JsonElement) {
        sendMessage(JsonElement.serializer(), message)
    }

    /**
     * @deprecated as of v4.0 Koltinx.serialization is used for serialization, please
     * use {@link ISocketContext#sendMessage(SerializationStrategy, Object)} and
     * {@link ISocketContext#sendMessage(JsonElement)}
     * @param message a message to send to the WebSocket client, it should be compatible with Jackson
     */
    fun sendMessage(message: Any)

    /**
     * @return the state of the context
     */
    val state: State

    /**
     * Closes this WebSocket
     */
    fun closeWebSocket()

    /**
     * @param closeCode the close code to send to the client
     */
    fun closeWebSocket(closeCode: Int)

    /**
     * @param closeCode the close code to send to the client
     * @param reason    the close reason to send to the client
     */
    fun closeWebSocket(closeCode: Int, reason: String?)
    enum class State {
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
