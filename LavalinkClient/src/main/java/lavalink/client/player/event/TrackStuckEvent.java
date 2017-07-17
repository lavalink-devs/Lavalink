package lavalink.client.player.event;

import lavalink.client.player.IPlayer;

public class TrackStuckEvent extends PlayerEvent {
    public TrackStuckEvent(IPlayer player) {
        super(player);
    }
}
