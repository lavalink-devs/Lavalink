package dev.arbjerg.lavalink.protocol.v4

import dev.arbjerg.lavalink.protocol.v4.serialization.SerializableHttpStatus
import dev.arbjerg.lavalink.protocol.v4.serialization.Timestamp
import dev.arbjerg.lavalink.protocol.v4.serialization.TimestampSerializer
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant
import io.ktor.http.HttpStatusCode

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
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Timestamp,
    val status: SerializableHttpStatus,
    val error: String,
    val trace: String? = null,
    val message: String,
    val path: String
)
