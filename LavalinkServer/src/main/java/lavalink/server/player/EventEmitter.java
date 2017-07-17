package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
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
    private final Player linkPlayer;

    EventEmitter(Player linkPlayer) {
        this.linkPlayer = linkPlayer;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackEndEvent");
        out.put("guildId", linkPlayer.getGuildId());
        try {
            out.put("track", Util.toMessage(track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        out.put("reason", endReason.toString());

        linkPlayer.getSocket().getSocket().send(out.toString());
    }

    // These exceptions are already logged by Lavaplayer
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackExceptionEvent");
        out.put("guildId", linkPlayer.getGuildId());
        try {
            out.put("track", Util.toMessage(track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        out.put("error", exception.getMessage());

        linkPlayer.getSocket().getSocket().send(out.toString());
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.warn(track.getInfo().title + " got stuck! Threshold surpassed: " + thresholdMs);

        JSONObject out = new JSONObject();
        out.put("op", "event");
        out.put("type", "TrackStuckEvent");
        out.put("guildId", linkPlayer.getGuildId());
        try {
            out.put("track", Util.toMessage(track));
        } catch (IOException e) {
            out.put("track", JSONObject.NULL);
        }

        out.put("thresholdMs", thresholdMs);

        linkPlayer.getSocket().getSocket().send(out.toString());
        SocketServer.sendPlayerUpdate(linkPlayer.getSocket().getSocket(), linkPlayer);
    }

}
