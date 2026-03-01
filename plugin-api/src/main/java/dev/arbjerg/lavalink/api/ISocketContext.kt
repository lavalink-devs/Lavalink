package dev.arbjerg.lavalink.api

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement

/**
 * Represents a WebSocket connection
 */
interface ISocketContext {
    /**
     * The session id of this socket connection.
     */
    val sessionId: String

    /**
     * The User ID of the Client.
     */
    val userId: Long

    /**
     * The name of this connections client if specified.
     */
    val clientName: String?

    /**
     * The User Agent of the Client if specified.
     */
    val userAgent: String?

    /**
     * A read-only map of all players associated by their guild.
     */
    val players: Map<Long, IPlayer>

    /**
     * Returns the player of a guild. Never returns null.
     *
     * @param guildId the guild the player is associated with
     * @return a potentially newly-created player
     */
    fun getPlayer(guildId: Long): IPlayer

    /**
     * Destroys the player for the Guild corresponding to [guildId].
     */
    fun destroyPlayer(guildId: Long)

    /**
     * Sends [message] to the WebSocket client
     * @param serializer a [SerializationStrategy] capable of serializing [T].
     */
    fun <T> sendMessage(serializer: SerializationStrategy<T>, message: T)

    /**
     * Sends [message] to the WebSocket client
     *
     * @see JsonElement
     */
    fun sendMessage(message: JsonElement) = sendMessage(JsonElement.serializer(), message)

    /**
     * The state of the context.
     */
    val state: State

    /**
     * Closes this WebSocket
     */
    fun closeWebSocket()

    /**
     * Closes this connection with [closeCode].
     */
    fun closeWebSocket(closeCode: Int)

    /**
     * Closes this connection with [closeCode] and [code].
     */
    fun closeWebSocket(closeCode: Int, reason: String?)

    /**
     * Possible states of a WebSocket connection.
     */
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
