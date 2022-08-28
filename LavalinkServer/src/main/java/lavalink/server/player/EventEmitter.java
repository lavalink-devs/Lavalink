/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
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

package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.server.io.SocketServer;
import lavalink.server.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EventEmitter extends AudioEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventEmitter.class);
    private final AudioPlayerManager audioPlayerManager;
    private final Player linkPlayer;

    EventEmitter(AudioPlayerManager audioPlayerManager, Player linkPlayer) {
        this.audioPlayerManager = audioPlayerManager;
        this.linkPlayer = linkPlayer;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackStartEvent");
        out.put("guildId", String.valueOf(linkPlayer.getGuildId()));

        try {
            out.put("track", Util.toMessage(audioPlayerManager, track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        linkPlayer.getSocket().send(out);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackEndEvent");
        out.put("guildId", String.valueOf(linkPlayer.getGuildId()));
        try {
            out.put("track", Util.toMessage(audioPlayerManager, track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        if (linkPlayer.getEndMarkerHit()) {
            out.put("reason", AudioTrackEndReason.FINISHED.toString());
            linkPlayer.setEndMarkerHit(false);
        } else {
            out.put("reason", endReason.toString());
        }

        linkPlayer.getSocket().send(out);
    }

    // These exceptions are already logged by Lavaplayer
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackExceptionEvent");
        out.put("guildId", String.valueOf(linkPlayer.getGuildId()));
        try {
            out.put("track", Util.toMessage(audioPlayerManager, track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        out.put("error", exception.getMessage());
        JSONObject exceptionJson = new JSONObject();
        exceptionJson.put("message", exception.getMessage());
        exceptionJson.put("severity", exception.severity.toString());
        exceptionJson.put("cause", Util.getRootCause(exception).toString());
        out.put("exception", exceptionJson);

        linkPlayer.getSocket().send(out);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.warn(track.getInfo().title + " got stuck! Threshold surpassed: " + thresholdMs);

        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackStuckEvent");
        out.put("guildId", String.valueOf(linkPlayer.getGuildId()));
        try {
            out.put("track", Util.toMessage(audioPlayerManager, track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        out.put("thresholdMs", thresholdMs);

        linkPlayer.getSocket().send(out);
        SocketServer.Companion.sendPlayerUpdate(linkPlayer.getSocket(), linkPlayer);
    }

}
