/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package lavalink.server.util

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.arbjerg.lavalink.protocol.v4.*
import kotlinx.serialization.json.JsonObject
import lavalink.server.io.SocketContext
import lavalink.server.io.SocketServer
import lavalink.server.player.LavalinkPlayer
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*


fun AudioTrack.toTrack(
    audioPlayerManager: AudioPlayerManager,
    pluginInfoModifiers: List<AudioPluginInfoModifier>
): Track {
    return this.toTrack(encodeTrack(audioPlayerManager, this), pluginInfoModifiers)
}

fun AudioTrack.toTrack(encoded: String, pluginInfoModifiers: List<AudioPluginInfoModifier>): Track {
    val pluginInfo = pluginInfoModifiers.fold(JsonObject(emptyMap())) { acc, it ->
        val jsonObject = it.modifyAudioTrackPluginInfo(this) ?: JsonObject(emptyMap())
        acc + jsonObject
    }

    return Track(encoded, this.toInfo(), pluginInfo, this.userData as? JsonObject ?: JsonObject(emptyMap()))
}

private operator fun JsonObject.plus(other: JsonObject) = JsonObject(toMap() + other.toMap())

fun AudioTrack.toInfo(): TrackInfo {
    return TrackInfo(
        this.identifier,
        this.isSeekable,
        this.info.author,
        this.duration,
        this.info.isStream,
        this.position,
        this.info.title,
        this.info.uri,
        this.sourceManager.sourceName,
        this.info.artworkUrl,
        this.info.isrc
    )
}

fun AudioPlaylist.toPlaylistInfo(): PlaylistInfo {
    return PlaylistInfo(this.name, if (this.selectedTrack == null) -1 else this.tracks.indexOf(this.selectedTrack))
}


fun AudioPlaylist.toPluginInfo(pluginInfoModifiers: List<AudioPluginInfoModifier>): JsonObject {
    val pluginInfo = pluginInfoModifiers.fold(JsonObject(emptyMap())) { acc, it ->
        val jsonObject = it.modifyAudioPlaylistPluginInfo(this) ?: JsonObject(emptyMap())
        acc + jsonObject
    }
    return pluginInfo
}

fun LavalinkPlayer.toPlayer(context: SocketContext, pluginInfoModifiers: List<AudioPluginInfoModifier>): Player {
    val connection = context.getMediaConnection(this).gatewayConnection
    val voiceServerInfo = context.koe.getConnection(guildId)?.voiceServerInfo

    return Player(
        guildId.toString(),
        track?.toTrack(context.audioPlayerManager, pluginInfoModifiers),
        audioPlayer.volume,
        audioPlayer.isPaused,
        PlayerState(
            System.currentTimeMillis(),
            track?.position ?: 0,
            connection?.isOpen ?: false,
            connection?.ping ?: -1
        ),
        VoiceState(
            voiceServerInfo?.token ?: "",
            voiceServerInfo?.endpoint ?: "",
            voiceServerInfo?.sessionId ?: ""
        ),
        filters.toFilters(),
    )
}


fun getRootCause(throwable: Throwable?): Throwable {
    var rootCause = throwable
    while (rootCause!!.cause != null) {
        rootCause = rootCause.cause
    }
    return rootCause
}

fun socketContext(socketServer: SocketServer, sessionId: String) =
    socketServer.sessions[sessionId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")

fun existingPlayer(socketContext: SocketContext, guildId: Long) =
    socketContext.players[guildId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

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

fun FriendlyException.Severity.toLavalink() = when (this) {
    FriendlyException.Severity.COMMON -> Exception.Severity.COMMON
    FriendlyException.Severity.SUSPICIOUS -> Exception.Severity.SUSPICIOUS
    FriendlyException.Severity.FAULT -> Exception.Severity.FAULT
}

fun Exception.Companion.fromFriendlyException(e: FriendlyException) = Exception(
    e.message,
    Exception.Severity.fromFriendlyException(e.severity),
    e.toString()
)

fun AudioTrackEndReason.toLavalink() = when (this) {
    AudioTrackEndReason.FINISHED -> Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED
    AudioTrackEndReason.LOAD_FAILED -> Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.LOAD_FAILED
    AudioTrackEndReason.STOPPED -> Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.STOPPED
    AudioTrackEndReason.REPLACED -> Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED
    AudioTrackEndReason.CLEANUP -> Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.CLEANUP
}

fun LoadResult.Companion.loadFailed(exception: FriendlyException) =
    loadFailed(Exception.fromFriendlyException(exception))
