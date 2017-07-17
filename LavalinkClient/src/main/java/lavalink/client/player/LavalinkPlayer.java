package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.PlayerPauseEvent;
import lavalink.client.player.event.PlayerResumeEvent;
import lavalink.client.player.event.TrackStartEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LavalinkPlayer implements IPlayer {

    private AudioTrack track = null;
    private boolean paused = false;
    private float volume = 1f;
    private long updateTime = -1;
    private long position = -1;

    private final LavalinkSocket socket;
    private final String guildId;
    private List<IPlayerEventListener> listeners = new ArrayList<>();

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

            emitEvent(new TrackStartEvent(this, track));
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
        if (b == paused) return;

        JSONObject json = new JSONObject();
        json.put("op", "pause");
        json.put("guildId", guildId);
        json.put("pause", b);
        socket.send(json.toString());

        if (b) {
            emitEvent(new PlayerPauseEvent(this));
        } else {
            emitEvent(new PlayerResumeEvent(this));
        }
    }

    @Override
    public boolean isPaused() {
        return paused;
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
    public void addListener(IPlayerEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IPlayerEventListener listener) {
        listeners.remove(listener);
    }

    public void emitEvent(PlayerEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }

}
