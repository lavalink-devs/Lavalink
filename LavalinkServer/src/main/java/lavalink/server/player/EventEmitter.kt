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
package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.Message
import lavalink.server.io.SocketServer.Companion.sendPlayerUpdate
import lavalink.server.util.getRootCause
import lavalink.server.util.toLavalink
import lavalink.server.util.toTrack
import org.slf4j.LoggerFactory

class EventEmitter(
    private val audioPlayerManager: AudioPlayerManager,
    private val player: LavalinkPlayer,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>
) : AudioEventAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(EventEmitter::class.java)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        this.player.socketContext.sendMessage(
            Message.Serializer,
            Message.EmittedEvent.TrackStartEvent(
                this.player.guildId.toString(),
                track.toTrack(audioPlayerManager, pluginInfoModifiers)
            )
        )
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val reason = if (this.player.endMarkerHit) {
            this.player.endMarkerHit = false
            AudioTrackEndReason.FINISHED
        } else {
            endReason
        }

        this.player.socketContext.sendMessage(
            Message.Serializer,
            Message.EmittedEvent.TrackEndEvent(
                this.player.guildId.toString(),
                track.toTrack(audioPlayerManager, pluginInfoModifiers),
                reason.toLavalink()
            )
        )
    }

    // These exceptions are already logged by Lavaplayer
    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        val rootCause = getRootCause(exception)

        this.player.socketContext.sendMessage(
            Message.Serializer,
            Message.EmittedEvent.TrackExceptionEvent(
                this.player.guildId.toString(),
                track.toTrack(audioPlayerManager, pluginInfoModifiers),
                Exception(exception.message, exception.severity.toLavalink(), rootCause.toString(), rootCause.stackTraceToString())
            )
        )
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        log.warn("${track.info.title} got stuck! Threshold surpassed: ${thresholdMs}ms")
        this.player.socketContext.sendMessage(
            Message.Serializer,
            Message.EmittedEvent.TrackStuckEvent(
                this.player.guildId.toString(),
                track.toTrack(audioPlayerManager, pluginInfoModifiers),
                thresholdMs
            )
        )
        sendPlayerUpdate(this.player.socketContext, this.player)
    }

}
