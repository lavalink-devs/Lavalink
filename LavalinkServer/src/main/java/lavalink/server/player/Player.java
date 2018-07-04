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

package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lavalink.plugin.IPlayer;
import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import net.dv8tion.jda.audio.AudioSendHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player extends AudioEventAdapter implements AudioSendHandler, IPlayer {

    private static final Logger log = LoggerFactory.getLogger(Player.class);

    private SocketContext socketContext;
    private final String guildId;
    private final AudioPlayer player;
    private AudioLossCounter audioLossCounter = new AudioLossCounter();
    private AudioFrame lastFrame = null;
    private ScheduledFuture myFuture = null;

    public Player(SocketContext socketContext, String guildId, AudioPlayerManager audioPlayerManager) {
        this.socketContext = socketContext;
        this.guildId = guildId;
        this.player = audioPlayerManager.createPlayer();
        this.player.addListener(this);
        this.player.addListener(new EventEmitter(audioPlayerManager, this));
        this.player.addListener(audioLossCounter);
    }

    @Override
    public AudioPlayer getAudioPlayer() {
        return player;
    }

    @Override
    public void play(AudioTrack track) {
        player.playTrack(track);
    }

    @Override
    public void stop() {
        player.stopTrack();
    }

    @Override
    public void setPause(boolean b) {
        player.setPaused(b);
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    @Override
    public void seekTo(long position) {
        player.getPlayingTrack().setPosition(position);
    }

    @Override
    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    @Override
    public JSONObject getState() {
        JSONObject json = new JSONObject();

        if (player.getPlayingTrack() != null)
            json.put("position", player.getPlayingTrack().getPosition());
        json.put("time", System.currentTimeMillis());

        return json;
    }

    @Override
    public AudioLossCounter getAudioLossCounter() {
        return audioLossCounter;
    }

    @Override
    public boolean isPlaying() {
        return player.getPlayingTrack() != null && !player.isPaused();
    }

    SocketContext getSocket() {
        return socketContext;
    }

    @Override
    public boolean canProvide() {
        lastFrame = player.provide();

        if(lastFrame == null) {
            audioLossCounter.onLoss();
            return false;
        } else {
            audioLossCounter.onSuccess();
            return true;
        }
    }

    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.getData();
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        myFuture.cancel(false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (myFuture == null || myFuture.isCancelled()) {
            myFuture = socketContext.playerUpdateService.scheduleAtFixedRate(() -> {
                SocketServer.sendPlayerUpdate(socketContext.getSocket(), this);
            }, 0, 5, TimeUnit.SECONDS);
        }
    }

}
