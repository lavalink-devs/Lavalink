package dev.arbjerg.lavalink.protocol.v3

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun decodeTrack(audioPlayerManager: AudioPlayerManager, message: String): AudioTrack {
    val bais = ByteArrayInputStream(Base64.decodeBase64(message))
    return audioPlayerManager.decodeTrack(MessageInput(bais)).decodedTrack
        ?: throw IllegalStateException("Failed to decode track due to a mismatching version or missing source manager")
}

// for backwards compatibility with Lavalink v3 we need to use the track format v2 to encode tracks for the v3 api
fun encodeTrack(track: AudioTrack): String {
    val baos = ByteArrayOutputStream()
    val stream = MessageOutput(baos)
    val output = stream.startMessage()
    // track info version 2
    output.write(2)

    output.writeUTF(track.info.title)
    output.writeUTF(track.info.author)
    output.writeLong(track.info.length)
    output.writeUTF(track.info.identifier)
    output.writeBoolean(track.info.isStream)
    DataFormatTools.writeNullableText(output, track.info.uri)

    val sourceManager = track.sourceManager
    output.writeUTF(sourceManager.sourceName)
    sourceManager.encodeTrack(track, output)

    output.writeLong(track.position)

    // track info versioned
    stream.commitMessage(1)

    return Base64.encodeBase64String(baos.toByteArray())
}
