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
    
    /**
     * @param player Audio player
     * @param track Audio track where the exception occurred
     */
    public void onTrackResolveError(IPlayer player, AudioTrack track) {
        // Adapter dummy method
    }


    @Override
    public void onEvent(PlayerEvent event) {
        if (event instanceof PlayerPauseEvent) {
            onPlayerPause(event.getPlayer());
        } else if (event instanceof PlayerResumeEvent) {
            onPlayerResume(event.getPlayer());
        } else if (event instanceof TrackStartEvent) {
            onTrackStart(event.getPlayer(), ((TrackStartEvent) event).getTrack());
        } else if (event instanceof TrackEndEvent) {
            onTrackEnd(event.getPlayer(), ((TrackEndEvent) event).getTrack(), ((TrackEndEvent) event).getReason());
        } else if (event instanceof TrackExceptionEvent) {
            onTrackException(event.getPlayer(), ((TrackExceptionEvent) event).getTrack(), ((TrackExceptionEvent) event).getException());
        } else if (event instanceof TrackStuckEvent) {
            onTrackStuck(event.getPlayer(), ((TrackStuckEvent) event).getTrack(), ((TrackStuckEvent) event).getThresholdMs());
        }else if (event instanceof TrackResolveErrorEvent) {
        	onTrackResolveError(event.getPlayer(), ((TrackResolveErrorEvent) event).getTrack());
        }
    }
}
