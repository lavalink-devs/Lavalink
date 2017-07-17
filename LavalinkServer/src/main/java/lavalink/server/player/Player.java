package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.server.Config;
import lavalink.server.Launcher;
import lavalink.server.io.SocketContext;
import lavalink.server.util.Util;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Player {

    private static final Logger log = LoggerFactory.getLogger(Player.class);

    public static final AudioPlayerManager PLAYER_MANAGER;

    static {
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        PLAYER_MANAGER.enableGcMonitoring();

        Config.Sources sources = Launcher.config.getSources();
        if (sources.isYoutube()) PLAYER_MANAGER.registerSourceManager(new YoutubeAudioSourceManager());
        if (sources.isBandcamp()) PLAYER_MANAGER.registerSourceManager(new BandcampAudioSourceManager());
        if (sources.isSoundcloud()) PLAYER_MANAGER.registerSourceManager(new SoundCloudAudioSourceManager());
        if (sources.isTwitch()) PLAYER_MANAGER.registerSourceManager(new TwitchStreamAudioSourceManager());
        if (sources.isVimeo()) PLAYER_MANAGER.registerSourceManager(new VimeoAudioSourceManager());
        if (sources.isHttp()) PLAYER_MANAGER.registerSourceManager(new HttpAudioSourceManager());
    }

    private final SocketContext socketContext;
    private final String guildId;
    private final AudioPlayer player = PLAYER_MANAGER.createPlayer();

    public Player(SocketContext socketContext, String guildId) {
        this.socketContext = socketContext;
        this.guildId = guildId;
    }

    public void play(AudioTrack track) {
        player.playTrack(track);
    }

    public void stop() {
        player.stopTrack();
    }

    public void setPause(boolean b) {
        player.setPaused(b);
    }

    public String getGuildId() {
        return guildId;
    }

    public JSONObject getState() {
        JSONObject json = new JSONObject();

        try {
            if (player.getPlayingTrack() != null ){
                json.put("playingTrack", Util.toMessage(player.getPlayingTrack()));
                json.put("position", player.getPlayingTrack().getPosition());
                json.put("duration", player.getPlayingTrack().getDuration());
            } else {
                json.put("playingTrack", JSONObject.NULL);
            }
            json.put("paused", player.isPaused());
            json.put("time", System.currentTimeMillis());

        } catch (IOException e) {
            throw new RuntimeException();
        }

        return json;
    }

}
