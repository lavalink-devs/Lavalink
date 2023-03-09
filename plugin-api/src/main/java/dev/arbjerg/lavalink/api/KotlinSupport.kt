package dev.arbjerg.lavalink.api

import kotlinx.serialization.serializer
import kotlinx.serialization.Serializable

/**
 * Sends [message] over the websocket.
 *
 * **Serialization required: This requires [T] to be annotated with [Serializable]**
 * @see ISocketContext
 */
inline fun <reified T : Any> ISocketContext.sendMessage(message: T) = sendMessage(serializer(), message)
