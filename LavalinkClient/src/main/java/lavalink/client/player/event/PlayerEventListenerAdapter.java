package lavalink.client.player.event;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;

public class PlayerEventListenerAdapter implements IPlayerEventListener {

    /**
     * @param player Audio player
     */
    public void onPlayerPause(IPlayer player) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     */
    public void onPlayerResume(IPlayer player) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track that started
     */
    public void onTrackStart(IPlayer player, AudioTrack track) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track that ended
     * @param endReason The reason why the track stopped playing
     */
    public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param exception The exception that occurred
     */
    public void onTrackException(IPlayer player, AudioTrack track, Exception exception) {
        // Adapter dummy method
    }

    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     * @param thresholdMs The wait threshold that was exceeded for this event to trigger
     */
    public void onTrackStuck(IPlayer player, AudioTrack track, long thresholdMs) {
        // Adapter dummy method
    }

    //TODO

    @Override
    public void onEvent(PlayerEvent event) {
        if (event instanceof PlayerPauseEvent) {
            onPlayerPause(event.getPlayer());
        } else if (event instanceof PlayerResumeEvent) {
            onPlayerResume(event.getPlayer());
        } else if (event instanceof TrackStartEvent) {
            onTrackStart(event.getPlayer(), null);
        } else if (event instanceof TrackEndEvent) {
            onTrackEnd(event.getPlayer(), null, null);
        } else if (event instanceof TrackExceptionEvent) {
            onTrackException(event.getPlayer(), null, null);
        } else if (event instanceof TrackStuckEvent) {
            onTrackStuck(event.getPlayer(), null, -1);
        }
    }
}
