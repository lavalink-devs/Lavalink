package dev.arbjerg.lavalink.api

import dev.arbjerg.lavalink.protocol.v4.LavalinkSerializersModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

/**
 * Sends [message] over the websocket.
 *
 * **Serialization required: This requires [T] to be annotated with [Serializable]**
 * @see ISocketContext
 */
inline fun <reified T : Any> ISocketContext.sendMessage(message: T) =
    sendMessage(LavalinkSerializersModule.serializer(), message)
