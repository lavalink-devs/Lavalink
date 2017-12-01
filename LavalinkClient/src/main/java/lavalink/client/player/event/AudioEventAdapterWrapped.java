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
            Exception e = ((TrackExceptionEvent) event).getException();
            FriendlyException fe = e instanceof FriendlyException
                    ? (FriendlyException) e
                    : new FriendlyException("Unexpected exception", FriendlyException.Severity.SUSPICIOUS, e);

            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent(player,
                    ((TrackExceptionEvent) event).getTrack(),
                    fe
            ));
        } else if (event instanceof TrackStuckEvent) {
            onEvent(new com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent(player,
                    ((TrackStuckEvent) event).getTrack(),
                    ((TrackStuckEvent) event).getThresholdMs()
            ));
        }
    }
}
