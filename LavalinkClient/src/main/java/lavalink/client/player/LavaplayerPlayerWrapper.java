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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lavalink.client.player.event.IPlayerEventListener;

public class LavaplayerPlayerWrapper implements IPlayer {

    private final AudioPlayer player;

    public LavaplayerPlayerWrapper(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    @Override
    public void playTrack(AudioTrack track) {
        player.playTrack(track);
    }

    @Override
    public void stopTrack() {
        player.stopTrack();
    }

    @Override
    public void setPaused(boolean b) {
        player.setPaused(b);
    }

    @Override
    public boolean isPaused() {
        return player.isPaused();
    }

    @Override
    public long getTrackPosition() {
        if (player.getPlayingTrack() == null) throw new IllegalStateException("Not playing anything");

        return player.getPlayingTrack().getPosition();
    }

    @Override
    public void seekTo(long position) {
        if (player.getPlayingTrack() == null) throw new IllegalStateException("Not playing anything");

        player.getPlayingTrack().setPosition(position);
    }

    @Override
    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    @Override
    public int getVolume() {
        return player.getVolume();
    }

    @Override
    public void addListener(IPlayerEventListener listener) {
        player.addListener((AudioEventListener) listener);
    }

    @Override
    public void removeListener(IPlayerEventListener listener) {
        player.removeListener((AudioEventListener) listener);
    }

    public AudioFrame provide() {
        return player.provide();
    }

    @Override
    public boolean isLoadingSong() {
        return false;
    }

}
