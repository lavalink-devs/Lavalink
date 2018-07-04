package lavalink.plugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;

public interface IPlayer {
    AudioPlayer getAudioPlayer();
    void play(AudioTrack track);
    void stop();
    void setPause(boolean b);
    String getGuildId();
    void seekTo(long position);
    void setVolume(int volume);
    JSONObject getState();
    IAudioLossCounter getAudioLossCounter();
    boolean isPlaying();
}
