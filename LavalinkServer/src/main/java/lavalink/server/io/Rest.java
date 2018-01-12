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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/{guildId}/connect")
    public void connect(@RequestParam String channelId,
                        @PathVariable String guildId,
                        @RequestHeader("Authorization") String identifier) {
        AudioManager audioManager = getCore(guildId, identifier).getAudioManager(guildId);
        if (audioManager.getConnectionListener() == null) {
            audioManager.setConnectionListener(new DebugConnectionListener(guildId));
        }
        audioManager.openAudioConnection(channelId);
    }

    @PostMapping("/{guildId}/disconnect")
    public void disconnect(@PathVariable String guildId,
                           @RequestHeader("Authorization") String identifier) {
        getCore(guildId, identifier).getAudioManager(guildId).closeAudioConnection();
    }

    @PostMapping("/{guildId}/play")
    public JSONObject play(@PathVariable String guildId,
                           @RequestBody String track,
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

    @PostMapping("/{guildId}/stop")
    public void stop(@PathVariable String guildId,
                     @RequestHeader("Authorization") String identifier) {
        getContext(identifier).getPlayer(guildId).stop();
    }

    @PostMapping("/{guildId}/pause")
    public void pause(@PathVariable String guildId,
                      @RequestParam boolean pause,
                      @RequestHeader("Authorization") String identifier) {
        SocketContext socketContext = getContext(identifier);
        Player player = socketContext.getPlayer(guildId);
        player.setPause(pause);
        sendPlayerUpdate(socketContext.getSocket(), player);
    }

    @PostMapping("/{guildId}/seek")
    public void seek(@PathVariable String guildId,
                     @RequestParam long position,
                     @RequestHeader("Authorization") String identifier) {
        SocketContext socketContext = getContext(identifier);
        Player player = socketContext.getPlayer(guildId);
        player.seekTo(position);
        sendPlayerUpdate(socketContext.getSocket(), player);
    }

    @PostMapping("/{guildId}/volume")
    public void volume(@PathVariable String guildId,
                       @RequestParam int volume,
                       @RequestHeader("Authorization") String identifier) {
        Player player = getContext(identifier).getPlayer(guildId);
        player.setVolume(volume);
    }

    @PostMapping("/{guildId}/destroy")
    public void destroy(@PathVariable String guildId,
                        @RequestHeader("Authorization") String identifier) {
        Player player = getContext(identifier).getPlayers().remove(guildId);
        if (player != null) player.stop();
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
