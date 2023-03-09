package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

/**
 * Represents an audio player for a specific guild. Contains track data and is used for controlling playback.
 */
interface IPlayer {
    /**
     * @return the underlying Lavaplayer player
     */
    val audioPlayer: AudioPlayer?

    val track: AudioTrack?

    /**
     * @return the guild which this player belongs to. Immutable
     */
    val guildId: Long

    /**
     * @return the WebSocket this player belongs to
     */
    val socketContext: ISocketContext

    /**
     * @param track the track to start playing, potentially replacing an existing one
     */
    fun play(track: AudioTrack)

    /**
     * Stops playing the current track, if any
     */
    fun stop()

    /**
     * @param pause whether to pause or not
     */
    fun setPause(pause: Boolean)

    /**
     * @param position the new position of the current track in milliseconds
     * @throws RuntimeException if not playing anything
     */
    fun seekTo(position: Long)

    /**
     * @param volume in percentage 0% to 1000%
     */
    fun setVolume(volume: Int)

    /**
     * @return Whether the player is trying to produce audio
     */
    val isPlaying: Boolean
}
