package lavalink.server.io;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import lavalink.server.player.Player;
import lavalink.server.player.TrackEndMarkerHandler;
import lavalink.server.util.DebugConnectionListener;
import lavalink.server.util.Util;
import net.dv8tion.jda.Core;
import net.dv8tion.jda.manager.AudioManager;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static lavalink.server.io.SocketServer.sendPlayerUpdate;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
@RestController
public class Rest {

    @PostMapping("/connect")
    public void connect(@RequestParam String channelId,
                        @RequestParam String guildId,
                        @RequestHeader("Authorization") String identifier) {
        AudioManager audioManager = getCore(guildId, identifier).getAudioManager(guildId);
        if (audioManager.getConnectionListener() == null) {
            audioManager.setConnectionListener(new DebugConnectionListener(guildId));
        }
        audioManager.openAudioConnection(channelId);
    }

    @PostMapping("/disconnect")
    public void disconnect(@RequestParam String guildId,
                           @RequestHeader("Authorization") String identifier) {
        getCore(guildId, identifier).getAudioManager(guildId).closeAudioConnection();
    }

    @PostMapping("/play")
    public JSONObject play(@RequestParam String guildId,
                           @RequestParam String track,
                           @RequestParam Long startTime,
                           @RequestParam Long endTime,
                           @RequestParam(required = false) Boolean paused,
                           @RequestHeader("Authorization") String identifier) throws IOException {
        Player player = getContext(identifier).getPlayer(guildId);
        AudioTrack audioTrack = Util.toAudioTrack(track);
        if (startTime != null) {
            audioTrack.setPosition(startTime);
        }
        if (endTime != null) {
            audioTrack.setMarker(new TrackMarker(endTime, new TrackEndMarkerHandler(player)));
        }

        player.setPause(paused != null ? paused : false);

        player.play(audioTrack);

        getCore(guildId, identifier).getAudioManager(guildId).setSendingHandler(getContext(identifier).getPlayer(guildId));
        sendPlayerUpdate(getContext(identifier).getSocket(), player);
        return player.getState();
    }

    @PostMapping("/stop")
    public void stop(@RequestParam String guildId,
                     @RequestHeader("Authorization") String identifier) {
        getContext(identifier).getPlayer(guildId).stop();
    }

    @PostMapping("/pause")
    public void pause(@RequestParam String guildId,
                      @RequestParam boolean pause,
                      @RequestHeader("Authorization") String identifier) {
        SocketContext socketContext = getContext(identifier);
        Player player = socketContext.getPlayer(guildId);
        player.setPause(pause);
        sendPlayerUpdate(socketContext.getSocket(), player);
    }

    @PostMapping("/seek")
    public void seek(@RequestParam String guildId,
                     @RequestParam long position,
                     @RequestHeader("Authorization") String identifier) {
        SocketContext socketContext = getContext(identifier);
        Player player = socketContext.getPlayer(guildId);
        player.seekTo(position);
        sendPlayerUpdate(socketContext.getSocket(), player);
    }

    @PostMapping("/volume")
    public void volume(@RequestParam String guildId,
                       @RequestParam int volume,
                       @RequestHeader("Authorization") String identifier) {
        Player player = getContext(identifier).getPlayer(guildId);
        player.setVolume(volume);
    }

    private Core getCore(String guildId, String identifier) {
        SocketContext socketContext = getContext(identifier);
        int shard = Util.getShardFromSnowflake(guildId, socketContext.getShardCount());
        return socketContext.getCore(shard);
    }

    private SocketContext getContext(String identifier) {
        SocketContext socketContext = SocketServer.getContext(identifier);
        if (socketContext == null) {
            throw new ForbiddenException();
        }
        return socketContext;
    }


}
