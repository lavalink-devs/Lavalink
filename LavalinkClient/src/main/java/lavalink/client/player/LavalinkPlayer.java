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
        return track;
    }

    @Override
    public void playTrack(AudioTrack track) {
        try {
            JSONObject json = new JSONObject();
            json.put("op", "play");
            json.put("guildId", guildId);
            json.put("track", LavalinkUtil.toMessage(track));
            socket.send(json.toString());
            position = 0;
            updateTime = System.currentTimeMillis();
            this.track = track;

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
        track = null;
    }

    @Override
    public void setPause(boolean pause) {
        if (pause == paused) return;

        JSONObject json = new JSONObject();
        json.put("op", "pause");
        json.put("guildId", guildId);
        json.put("pause", pause);
        socket.send(json.toString());
        paused = pause;

        if (pause) {
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
        if (getPlayingTrack() == null) throw new IllegalStateException("Not currently playing anything");
        if (getPlayingTrack().getInfo().isStream) return Long.MAX_VALUE;

        long timeDiff = System.currentTimeMillis() - updateTime;

        return Math.min(position + timeDiff, track.getDuration());
    }

    @Override
    public void seekTo(long position) {
        if (getPlayingTrack() == null)  throw new IllegalStateException("Not currently playing anything");
        if (getPlayingTrack().getInfo().isStream)  throw new IllegalStateException("Can't seek in a stream");

        JSONObject json = new JSONObject();
        json.put("op", "seek");
        json.put("guildId", guildId);
        json.put("position", position);
        socket.send(json.toString());
    }

    @Override
    public void setVolume(int volume) {
        volume = Math.min(150, Math.max(0, volume)); // Lavaplayer bounds

        JSONObject json = new JSONObject();
        json.put("op", "volume");
        json.put("guildId", guildId);
        json.put("volume", volume);
        socket.send(json.toString());
        this.volume = volume;
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
