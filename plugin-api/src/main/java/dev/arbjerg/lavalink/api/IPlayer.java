package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Represents an audio player for a specific guild. Contains track data and is used for controlling playback.
 */
public interface IPlayer {
    /**
     * @return the underlying Lavaplyer player
     */
    AudioPlayer getAudioPlayer();

    /**
     * @return the player's current track which is either playing or paused. May be null
     */
    AudioTrack getTrack();

    /**
     * @return the guild which this player belongs to. Immutable
     */
    long getGuildId();

    /**
     * @return the WebSocket this player belongs to
     */
    ISocketContext getSocketContext();

    /**
     * @param track the track to start playing, potentially replacing an existing one
     */
    void play(AudioTrack track);

    /**
     * Stops playing the current track, if any
     */
    void stop();

    /**
     * @param pause whether to pause or not
     */
    void setPause(boolean pause);

    /**
     * @param position the new position of the current track in milliseconds
     * @throws RuntimeException if not playing anything
     */
    void seekTo(long position);

    /**
     * @param volume in percentage 0% to 1000%
     */
    void setVolume(int volume);

    /**
     * @return Whether the player is trying to produce audio
     */
    boolean isPlaying();
}
