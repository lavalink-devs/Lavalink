package lavalink.api;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface IPlayer {
    AudioPlayer getAudioPlayer();
    long getGuildId();
    ISocketContext getSocketContext();
    void play(AudioTrack track);
    void stop();
    void setPause(boolean pause);
    void seekTo(long position);
    /** @param volume, in percentage */
    void setVolume(int volume);
    boolean isPlaying();
}
