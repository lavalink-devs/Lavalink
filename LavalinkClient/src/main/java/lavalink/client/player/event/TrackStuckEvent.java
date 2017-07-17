package lavalink.client.player.event;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;

public class TrackStuckEvent extends PlayerEvent {

    private AudioTrack track;
    private long thresholdMs;

    public TrackStuckEvent(IPlayer player, AudioTrack track, long thresholdMs) {
        super(player);
        this.track = track;
        this.thresholdMs = thresholdMs;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public long getThresholdMs() {
        return thresholdMs;
    }
}
