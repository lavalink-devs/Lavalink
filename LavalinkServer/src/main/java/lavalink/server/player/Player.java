package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import lavalink.server.Config;
import lavalink.server.Launcher;
import lavalink.server.io.SocketContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player {

    private static final Logger log = LoggerFactory.getLogger(Player.class);

    static final AudioPlayerManager PLAYER_MANAGER;

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

    public Player(SocketContext socketContext, String guildId) {
        this.socketContext = socketContext;
        this.guildId = guildId;
    }
}
