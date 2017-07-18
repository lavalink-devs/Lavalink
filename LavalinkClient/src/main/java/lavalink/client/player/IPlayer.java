package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.event.IPlayerEventListener;

public interface IPlayer {

    AudioTrack getPlayingTrack();

    void playTrack(AudioTrack track);

    void stopTrack();

    void setPaused(boolean b);

    boolean isPaused();

    long getTrackPosition();

    void seekTo(long position);

    void setVolume(int volume);

    void addListener(IPlayerEventListener listener);

    void removeListener(IPlayerEventListener listener);

}
