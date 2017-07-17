package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface IPlayer {

    AudioTrack getPlayingTrack();

    void playTrack(AudioTrack track);

    void stop();

    void setPause(boolean b);

    boolean isPaused();

    long getTrackPosition();

    long getTrackDuration();

    void seekTo(long position);

    void setVolume(float volume);

    void addListener(AudioEventListener listener);

    void removeListener(AudioEventListener listener);

}
