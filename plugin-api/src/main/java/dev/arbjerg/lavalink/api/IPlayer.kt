package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.time.Duration

/**
 * Represents an audio player for a specific guild. Contains track data and is used for controlling playback.
 */
interface IPlayer {
    /**
     * @return the underlying Lavaplayer player
     */
    val audioPlayer: AudioPlayer

    /**
     * The currently playing track if any.
     */
    val track: AudioTrack?

    /**
     * The guild which this player belongs to. Immutable
     */
    val guildId: Long

    /**
     * The WebSocket this player belongs to
     */
    val socketContext: ISocketContext

    /**
     * Whether the player is trying to produce audio.
     */
    val isPlaying: Boolean

    /**
     * Starts playing [track], potentially replacing an existing one.
     */
    fun play(track: AudioTrack)

    /**
     * Stops playing the current track, if any.
     */
    fun stop()

    /**
     * Sets the pause state to [pause].
     */
    fun setPause(pause: Boolean)

    /**
     * Seeks to [position].
     *
     * @param position the new position of the current track in milliseconds
     * @throws RuntimeException if not playing anything
     */
    fun seekTo(position: Long)

    /**
     * Seeks to [position].
     *
     * @throws RuntimeException if not playing anything
     */
    fun seekTo(position: Duration) = seekTo(position.inWholeMilliseconds)

    /**
     * Sets the volume to [volume].
     * @param volume in percentage 0% to 1000%
     */
    fun setVolume(volume: Int)
}
