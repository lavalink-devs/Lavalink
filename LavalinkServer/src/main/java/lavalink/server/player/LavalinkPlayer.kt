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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import dev.arbjerg.lavalink.api.IPlayer
import dev.arbjerg.lavalink.api.ISocketContext
import io.netty.buffer.ByteBuf
import lavalink.server.config.ServerConfig
import lavalink.server.io.SocketContext
import lavalink.server.io.SocketServer.Companion.sendPlayerUpdate
import lavalink.server.player.filters.FilterChain
import moe.kyokobot.koe.MediaConnection
import moe.kyokobot.koe.media.OpusAudioFrameProvider
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class LavalinkPlayer(
    val socket: SocketContext,
    private val guildId: Long,
    private val serverConfig: ServerConfig,
    audioPlayerManager: AudioPlayerManager,
) : AudioEventAdapter(), IPlayer {
    val audioLossCounter = AudioLossCounter()
    var endMarkerHit = false
    var filters: FilterChain = FilterChain()
        set(value) {
            audioPlayer.setFilterFactory(value.takeIf { it.isEnabled })
            field = value
        }

    private val audioPlayer: AudioPlayer = audioPlayerManager.createPlayer().also {
        it.addListener(this)
        it.addListener(EventEmitter(audioPlayerManager, this))
        it.addListener(audioLossCounter)
    }

    private var updateFuture: ScheduledFuture<*>? = null

    fun destroy() {
        audioPlayer.destroy()
    }

    fun provideTo(connection: MediaConnection) {
        connection.audioSender = Provider(connection)
    }

    override fun isPlaying(): Boolean = audioPlayer.playingTrack != null && !audioPlayer.isPaused

    override fun getAudioPlayer(): AudioPlayer = audioPlayer

    override fun getTrack(): AudioTrack? = audioPlayer.playingTrack

    override fun getGuildId(): Long = guildId

    override fun getSocketContext(): ISocketContext = socket

    override fun play(track: AudioTrack) {
        audioPlayer.playTrack(track)
        sendPlayerUpdate(socket, this)
    }

    override fun stop() {
        audioPlayer.stopTrack()
    }

    override fun setPause(b: Boolean) {
        audioPlayer.isPaused = b
    }

    override fun seekTo(position: Long) {
        val track = audioPlayer.playingTrack ?: throw RuntimeException("Can't seek when not playing anything")
        track.position = position
    }

    override fun setVolume(volume: Int) {
        audioPlayer.volume = volume
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        updateFuture!!.cancel(false)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        if (updateFuture?.isCancelled == false) {
            return
        }

        updateFuture = socket.playerUpdateService.scheduleAtFixedRate(
            { sendPlayerUpdate(socket, this) },
            0,
            serverConfig.playerUpdateInterval.toLong(),
            TimeUnit.SECONDS
        )
    }

    private inner class Provider(connection: MediaConnection?) : OpusAudioFrameProvider(connection) {
        private var lastFrame: AudioFrame? = null

        override fun canProvide(): Boolean {
            lastFrame = audioPlayer.provide()
            return if (lastFrame == null) {
                audioLossCounter.onLoss()
                false
            } else {
                true
            }
        }

        override fun retrieveOpusFrame(buf: ByteBuf) {
            audioLossCounter.onSuccess()
            buf.writeBytes(lastFrame!!.data)
        }
    }
}
