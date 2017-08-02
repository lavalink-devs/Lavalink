/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.LavalinkUtil;
import lavalink.client.io.Lavalink;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.player.event.IPlayerEventListener;
import lavalink.client.player.event.PlayerEvent;
import lavalink.client.player.event.PlayerPauseEvent;
import lavalink.client.player.event.PlayerResumeEvent;
import lavalink.client.player.event.TrackStartEvent;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LavalinkPlayer implements IPlayer {

    private AudioTrack track = null;
    private boolean paused = false;
    private int volume = 100;
    private long updateTime = -1;
    private long position = -1;

    private Lavalink lavalink;
    private LavalinkSocket socket;
    private final String guildId;
    private List<IPlayerEventListener> listeners = new ArrayList<>();

    public LavalinkPlayer(Lavalink lavalink, LavalinkSocket socket, String guildId) {
        this.lavalink = lavalink;
        this.socket = socket;
        this.guildId = guildId;
        addListener(new LavalinkInternalPlayerEventHandler());
    }

    public void setSocket(LavalinkSocket socket) {
        this.socket = socket;

        JDAImpl jda = (JDAImpl) lavalink.getShard(LavalinkUtil.getShardFromSnowflake(guildId, lavalink.getNumShards()));
        //jda.getGuildById(guildId).getAudioManager().closeAudioConnection();

        // Close the audio connection by force if it exists
        jda.getClient().send("{\"op\":4,\"d\":{\"self_deaf\":false,\"guild_id\":\"" + guildId + "\",\"channel_id\":null,\"self_mute\":false}}");

        // Make sure we are actually connected to a VC
        if (socket != null
                && lavalink.getConnectedChannel(guildId) != null) {
            int shardId = LavalinkUtil.getShardFromSnowflake(guildId, lavalink.getNumShards());
            JDA shard = lavalink.getShard(shardId);
            lavalink.openVoiceConnection(lavalink.getConnectedChannel(shard.getGuildById(guildId)));
        }
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
            if (socket != null)
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
    public void stopTrack() {
        JSONObject json = new JSONObject();
        json.put("op", "stop");
        json.put("guildId", guildId);
        if (socket != null)
            socket.send(json.toString());
        track = null;
    }

    @Override
    public void setPaused(boolean pause) {
        if (pause == paused) return;

        JSONObject json = new JSONObject();
        json.put("op", "pause");
        json.put("guildId", guildId);
        json.put("pause", pause);
        if (socket != null)
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
        if (getPlayingTrack() == null) throw new IllegalStateException("Not currently playing anything");
        if (getPlayingTrack().getInfo().isStream) throw new IllegalStateException("Can't seek in a stream");

        JSONObject json = new JSONObject();
        json.put("op", "seek");
        json.put("guildId", guildId);
        json.put("position", position);
        if (socket != null)
            socket.send(json.toString());
    }

    @Override
    public void setVolume(int volume) {
        volume = Math.min(150, Math.max(0, volume)); // Lavaplayer bounds

        JSONObject json = new JSONObject();
        json.put("op", "volume");
        json.put("guildId", guildId);
        json.put("volume", volume);
        if (socket != null)
            socket.send(json.toString());
        this.volume = volume;
    }

    @Override
    public int getVolume() {
        return volume;
    }

    public void provideState(JSONObject json) {
        updateTime = json.getLong("time");
        position = json.getLong("position");
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

    void clearTrack() {
        track = null;
    }

}
