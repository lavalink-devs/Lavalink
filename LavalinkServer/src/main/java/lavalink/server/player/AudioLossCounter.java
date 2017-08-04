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
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioLossCounter extends AudioEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(AudioLossCounter.class);

    public static final int EXPECTED_PACKET_COUNT_PER_MIN = (60 * 1000) / 20; // 20ms packets
    private static final int ACCEPTABLE_TRACK_SWITCH_TIME = 100; //ms

    private long curMinute = 0;
    private int curLoss = 0;
    private int curSucc = 0;

    private int lastLoss = 0;
    private int lastSucc = 0;

    private long playingSince = Long.MAX_VALUE;
    private long lastTrackStarted = Long.MAX_VALUE / 2;
    private long lastTrackEnded = Long.MAX_VALUE;

    AudioLossCounter() {
    }

    void onLoss() {
        checkTime();
        curLoss++;
    }

    void onSuccess() {
        checkTime();
        curSucc++;
    }

    public int getLastMinuteLoss() {
        return lastLoss;
    }

    public int getLastMinuteSuccess() {
        return lastSucc;
    }

    public boolean isDataUsable() {
        //log.info("\n" + lastTrackStarted + "\n" + lastTrackEnded + "\n" + playingSince);

        // Check that there isn't a significant gap in playback. If no track has ended yet, we can look past that
        if(lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME
                && lastTrackEnded != Long.MAX_VALUE ) return false;

        // Check that we have at least stats for last minute
        long lastMin = System.currentTimeMillis() / 60000 - 1;
        //log.info((playingSince < lastMin * 60000) + "");
        return playingSince < lastMin * 60000;
    }

    private void checkTime() {
        long actualMinute = System.currentTimeMillis() / 60000;

        if(curMinute != actualMinute) {
            lastLoss = curLoss;
            lastSucc = curSucc;
            curLoss = 0;
            curSucc = 0;
            curMinute = actualMinute;
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer __, AudioTrack ___, AudioTrackEndReason ____) {
        lastTrackEnded = System.currentTimeMillis();
    }

    @Override
    public void onTrackStart(AudioPlayer __, AudioTrack ___) {
        lastTrackStarted = System.currentTimeMillis();

        if (lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME
                || playingSince == Long.MAX_VALUE) {
            playingSince = System.currentTimeMillis();
            lastTrackEnded = Long.MAX_VALUE;
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        onTrackEnd(null, null, null);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        onTrackStart(null, null);
    }

    @Override
    public String toString() {
        return "AudioLossCounter{" +
                "lastLoss=" + lastLoss +
                ", lastSucc=" + lastSucc +
                ", total=" + (lastSucc + lastLoss) +
                '}';
    }
}
