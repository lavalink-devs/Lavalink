package dev.arbjerg.lavalink.protocol.v4

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

fun decodeTrack(audioPlayerManager: AudioPlayerManager, message: String): AudioTrack {
    val bais = ByteArrayInputStream(Base64.getDecoder().decode(message))
    return audioPlayerManager.decodeTrack(MessageInput(bais)).decodedTrack
        ?: throw IllegalStateException("Failed to decode track due to a mismatching version or missing source manager")
}

fun encodeTrack(audioPlayerManager: AudioPlayerManager, track: AudioTrack): String {
    val baos = ByteArrayOutputStream()
    audioPlayerManager.encodeTrack(MessageOutput(baos), track)
    return Base64.getEncoder().encodeToString(baos.toByteArray())
}

fun Exception.Severity.Companion.fromFriendlyException(e: FriendlyException.Severity) = when (e) {
    FriendlyException.Severity.COMMON -> Exception.Severity.COMMON
    FriendlyException.Severity.SUSPICIOUS -> Exception.Severity.SUSPICIOUS
    FriendlyException.Severity.FAULT -> Exception.Severity.FAULT
}

fun Exception.Companion.fromFriendlyException(e: FriendlyException) = Exception(
    e.message,
    Exception.Severity.fromFriendlyException(e.severity),
    e.toString()
)

fun LoadResult.Companion.loadFailed(exception: FriendlyException) =
    loadFailed(Exception.fromFriendlyException(exception))
