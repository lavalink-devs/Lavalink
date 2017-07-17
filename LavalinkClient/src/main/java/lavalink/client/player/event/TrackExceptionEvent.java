package lavalink.client.player.event;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;

public class TrackExceptionEvent extends PlayerEvent {

    private AudioTrack track;
    private Exception exception;

    public TrackExceptionEvent(IPlayer player, AudioTrack track, Exception exception) {
        super(player);
        this.track = track;
        this.exception = exception;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Exception getException() {
        return exception;
    }
}
