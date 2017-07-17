package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.LavalinkSocket;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LavalinkPlayer implements IPlayer {

    private JSONObject audioState = null;
    private final LavalinkSocket socket;
    private final String guildId;
    private List<AudioEventListener> listeners = new ArrayList<>();

    public LavalinkPlayer(LavalinkSocket socket, String guildId) {
        this.socket = socket;
        this.guildId = guildId;
    }


    @Override
    public AudioTrack getPlayingTrack() {
        return null; //TODO
    }

    @Override
    public void playTrack(AudioTrack track) {
        try {
            JSONObject json = new JSONObject();
            json.put("op", "play");
            json.put("guildId", guildId);
            json.put("track", LavalinkUtil.toMessage(track));
            socket.send(json.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void stop() {
        JSONObject json = new JSONObject();
        json.put("op", "stop");
        json.put("guildId", guildId);
        socket.send(json.toString());
    }

    @Override
    public void setPause(boolean b) {
        JSONObject json = new JSONObject();
        json.put("op", "pause");
        json.put("guildId", guildId);
        json.put("pause", b);
        socket.send(json.toString());
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public long getTrackPosition() {
        return 0;
    }

    @Override
    public long getTrackDuration() {
        return 0;
    }

    @Override
    public void seekTo(long position) {

    }

    @Override
    public void setVolume(float volume) {

    }

    @Override
    public void addListener(AudioEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(AudioEventListener listener) {
        listeners.remove(listener);
    }

    public void providePlayerState(JSONObject state) {

    }

}
