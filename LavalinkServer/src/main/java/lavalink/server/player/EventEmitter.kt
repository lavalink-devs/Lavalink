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
import dev.arbjerg.lavalink.protocol.v3.Exception
import dev.arbjerg.lavalink.protocol.v3.Message
import dev.arbjerg.lavalink.protocol.v3.encodeTrack
import lavalink.server.io.SocketServer.Companion.sendPlayerUpdate
import lavalink.server.util.getRootCause
import org.slf4j.LoggerFactory

class EventEmitter(
    private val audioPlayerManager: AudioPlayerManager,
    private val player: LavalinkPlayer
) : AudioEventAdapter() {

    companion object {
        private val log = LoggerFactory.getLogger(EventEmitter::class.java)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val encodedTrack = encodeTrack(audioPlayerManager, track)
        this.player.socket.sendMessage(
            Message.TrackStartEvent(encodedTrack, encodedTrack, this.player.guildId.toString())
        )
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        val reason = if (this.player.endMarkerHit) {
            this.player.endMarkerHit = false
            AudioTrackEndReason.FINISHED
        } else {
            endReason
        }

        val encodedTrack = encodeTrack(audioPlayerManager, track)
        this.player.socket.sendMessage(
            Message.TrackEndEvent(encodedTrack, encodedTrack, reason, this.player.guildId.toString())
        )
    }

    // These exceptions are already logged by Lavaplayer
    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        val encodedTrack = encodeTrack(audioPlayerManager, track)
        this.player.socket.sendMessage(
            Message.TrackExceptionEvent(
                encodedTrack,
                encodedTrack,
                Exception(exception.message, exception.severity, getRootCause(exception).toString()),
                this.player.guildId.toString()
            )
        )
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        log.warn("${track.info.title} got stuck! Threshold surpassed: ${thresholdMs}ms")
        val encodedTrack = encodeTrack(audioPlayerManager, track)
        this.player.socket.sendMessage(
            Message.TrackStuckEvent(encodedTrack, encodedTrack, thresholdMs, this.player.guildId.toString())
        )
        sendPlayerUpdate(this.player.socket, this.player)
    }

}
