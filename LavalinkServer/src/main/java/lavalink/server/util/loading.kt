package lavalink.server.util

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem

/**
 * Loads an audio item from the specified [identifier].
 *
 * This method wraps any exceptions thrown by the [AudioPlayerManager.loadItem] method in a [FriendlyException]. 
 * This is meant to maintain the behavior from callback-style item loading.
 */
fun loadAudioItem(manager: AudioPlayerManager, identifier: String): AudioItem? = try {
    manager.loadItemSync(identifier)
} catch (ex: Throwable) {
    // re-throw any errors that are not exceptions
    ExceptionTools.rethrowErrors(ex)

    throw FriendlyException(
        "Something went wrong while looking up the track.",
        FriendlyException.Severity.FAULT,
        ex
    )
}
