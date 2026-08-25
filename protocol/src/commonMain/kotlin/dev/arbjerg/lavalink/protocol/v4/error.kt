@file:Suppress("DEPRECATION")
package dev.arbjerg.lavalink.protocol.v4

import dev.arbjerg.lavalink.protocol.v4.serialization.Timestamp
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toStdlibInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlinx.datetime.Instant as DeprecatedInstant

/**
 * Representation of a REST error.
 *
 * @property timestamp the [timestamp][Instant] of the error in milliseconds since the epoch
 * @property status the HTTP status code
 * @property error The HTTP status code message
 * @property trace The stack trace of the error when `trace=true` as query param has been sent
 * @property message The error message
 * @property path The request path
 */
@Serializable
data class Error(
    @SerialName("timestamp")
    val instant: Timestamp,
    val status: Int,
    val error: String,
    val trace: String? = null,
    val message: String,
    val path: String
) {

    /**
     * Old constructor kept for binary compatibility.
     *
     * @deprecated Use the primary constructor instead which uses [Instant] instead of [DeprecatedInstant].
     */
    @Deprecated("Replaced by the new instant variable, which uses kotlin.time.Instant rather than kotlinx.datetime.Instant")
    constructor(
        instant: DeprecatedInstant,
        status: Int,
        error: String,
        trace: String? = null,
        message: String,
        path: String
    ) : this(instant.toStdlibInstant(), status, error, trace, message, path)

    /**
     * Old copy function kept for binary compatibility.
     *
     * @deprecated Use the new copy function instead which uses [Instant] instead of [DeprecatedInstant].
     */
    @Deprecated("Replaced by the new instant variable, which uses kotlin.time.Instant rather than kotlinx.datetime.Instant")
    fun copy(
        instant: DeprecatedInstant = this.instant.toDeprecatedInstant(),
        status: Int = this.status,
        error: String = this.error,
        trace: String? = this.trace,
        message: String = this.message,
        path: String = this.path,
    ) = Error(instant, status, error, trace, message, path)

    /**
     * Old accessor for timestamp only kept for binary compatibility.
     *
     * @deprecated Use [instant] instead.
     * @see instant
     */
    @Deprecated(
        "Replaced by the new instant variable, which uses kotlin.time.Instant rather than kotlinx.datetime.Instant",
        ReplaceWith("instant")
    )
    val timestamp by lazy { instant.toDeprecatedInstant() }
}
