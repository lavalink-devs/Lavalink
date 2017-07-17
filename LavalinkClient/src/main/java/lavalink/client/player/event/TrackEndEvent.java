package lavalink.client.player.event;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;

public class TrackEndEvent extends PlayerEvent {

    private AudioTrack track;
    private AudioTrackEndReason reason;

    public TrackEndEvent(IPlayer player, AudioTrack track, AudioTrackEndReason reason) {
        super(player);
        this.track = track;
        this.reason = reason;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public AudioTrackEndReason getReason() {
        return reason;
    }
}
