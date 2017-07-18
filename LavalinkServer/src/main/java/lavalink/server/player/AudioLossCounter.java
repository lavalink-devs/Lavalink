package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class AudioLossCounter extends AudioEventAdapter {

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
        // Check that there isn't a significant gap in playback. If no track has ended yet, we can look past that
        if(lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME
                && lastTrackEnded != Long.MAX_VALUE) return false;

        // Check that we have at least stats for last minute
        long lastMin = System.currentTimeMillis() / 60000 - 1;
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
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        lastTrackEnded = System.currentTimeMillis();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        lastTrackStarted = System.currentTimeMillis();

        if (lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME) {
            playingSince = System.currentTimeMillis();
        }
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
