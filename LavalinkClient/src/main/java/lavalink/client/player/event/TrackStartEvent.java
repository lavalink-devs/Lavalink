package lavalink.client.player.event;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;

public class TrackStartEvent extends PlayerEvent {

    private AudioTrack track;

    public TrackStartEvent(IPlayer player, AudioTrack track) {
        super(player);
        this.track = track;
    }

    public AudioTrack getTrack() {
        return track;
    }
}
