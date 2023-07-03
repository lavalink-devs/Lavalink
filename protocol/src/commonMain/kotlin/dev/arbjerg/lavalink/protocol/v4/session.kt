package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Representation of a session.
 *
 * @property resuming whether resuming is enabled or not
 * @property timeout [Duration] you are allowed to resume
 * @property timeoutSeconds amount of seconds you are allowed to resume
 */
@Serializable
data class Session(
    val resuming: Boolean,
    @SerialName("timeout")
    val timeoutSeconds: Long,
) {
    val timeout: Duration by lazy { timeoutSeconds.toDuration(DurationUnit.SECONDS) }

    companion object {
        operator fun invoke(resuming: Boolean, timeout: Duration) = Session(
            resuming,
            timeout.inWholeSeconds
        )
    }
}

/**
 * Request used to update a session.
 *
 * @property resuming whether resuming is enabled or not
 * @property timeout [Duration] you are allowed to resume
 * @property timeoutSeconds amount of seconds you are allowed to resume
 */
@Serializable
data class SessionUpdate(
    val resuming: Omissible<Boolean> = Omissible.Omitted(),
    @SerialName("timeout")
    val timeoutSeconds: Omissible<Long> = Omissible.Omitted(),
) {
    val timeout: Omissible<Duration> by lazy { timeoutSeconds.map { it.toDuration(DurationUnit.SECONDS) } }

    companion object {
        operator fun invoke(resuming: Omissible<Boolean>, timeout: Omissible<Duration>) = SessionUpdate(
            resuming,
            timeout.map(Duration::inWholeSeconds)
        )
    }
}
