package lavalink.client.player.event;

import lavalink.client.player.IPlayer;

public class PlayerPauseEvent extends PlayerEvent {
    public PlayerPauseEvent(IPlayer player) {
        super(player);
    }
}
