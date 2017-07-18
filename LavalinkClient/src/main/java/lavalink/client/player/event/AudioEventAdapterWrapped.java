package lavalink.client.player.event;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;


/**
 * A lavaplayer AudioEventAdapter that is also an IPlayerEventListener.
 * This is used for abstracting between Lavaplayer and Lavalink
 */
public abstract class AudioEventAdapterWrapped extends AudioEventAdapter implements IPlayerEventListener {

    private AudioPlayer player;

    @Override
    public void onEvent(PlayerEvent event) {
        if (event instanceof PlayerPauseEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent(player));
        } else if (event instanceof PlayerResumeEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent(player));
        } else if (event instanceof TrackStartEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent(player,
                    ((TrackStartEvent) event).getTrack()));
        } else if (event instanceof TrackEndEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent(player,
                    ((TrackEndEvent) event).getTrack(),
                    ((TrackEndEvent) event).getReason()
                    ));
        } else if (event instanceof TrackExceptionEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent(player,
                    ((TrackExceptionEvent) event).getTrack(),
                    (FriendlyException) ((TrackExceptionEvent) event).getException()
            ));
        } else if (event instanceof TrackStuckEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent(player,
                    ((TrackStuckEvent) event).getTrack(),
                    ((TrackStuckEvent) event).getThresholdMs()
            ));
        }
    }
}
