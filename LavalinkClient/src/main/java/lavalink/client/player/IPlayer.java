package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface IPlayer {

    AudioTrack getPlayingTrack();

    void playTrack(AudioTrack track);

    void stop();

    void setPause(boolean b);

}
