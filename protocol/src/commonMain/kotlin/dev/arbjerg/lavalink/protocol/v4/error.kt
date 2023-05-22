package dev.arbjerg.lavalink.protocol.v4

import dev.arbjerg.lavalink.protocol.v4.serialization.Timestamp
import dev.arbjerg.lavalink.protocol.v4.serialization.TimestampSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Representation of a REST error.
 *
 * @property timestamp the [timestamp][Instant] of the error in milliseconds since the epoch
 * @property status the [HTTP status code][HttpStatusCode]
 * @property error The HTTP status code message
 * @property trace The stack trace of the error when `trace=true` as query param has been sent
 * @property message The error message
 * @property path The request path
 */
@Serializable
data class Error(
    val timestamp: Timestamp,
    val status: Int,
    val error: String,
    val trace: String? = null,
    val message: String,
    val path: String
)
