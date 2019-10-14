package lavalink.server.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.Ipv6Block;
import com.sedmelluq.discord.lavaplayer.tools.http.BalancingIpv6RoutePlanner;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Created by napster on 05.03.18.
 */
@Component
public class AudioPlayerConfiguration {

    @Bean
    public Supplier<AudioPlayerManager> audioPlayerManagerSupplier(AudioSourcesConfig sources, ServerConfig serverConfig) {
        return () -> {
            AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

            if (serverConfig.isGcWarnings()) {
                audioPlayerManager.enableGcMonitoring();
            }

            if (sources.isYoutube()) {
                YoutubeAudioSourceManager youtube;
                if (!serverConfig.getBalancingBlock().isEmpty()) {
                    HttpRoutePlanner planner = new BalancingIpv6RoutePlanner(new Ipv6Block(serverConfig.getBalancingBlock()));
                    youtube = new YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled(), planner);
                } else {
                    youtube = new YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled());
                }
                Integer playlistLoadLimit = serverConfig.getYoutubePlaylistLoadLimit();

                if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit);

                audioPlayerManager.registerSourceManager(youtube);
            }
            if (sources.isBandcamp()) audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
            if (sources.isSoundcloud()) audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager(serverConfig.isSoundcloudSearchEnabled()));
            if (sources.isTwitch()) audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
            if (sources.isVimeo()) audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
            if (sources.isMixer()) audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
            if (sources.isHttp()) audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
            if (sources.isLocal()) audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());

            audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);

            return audioPlayerManager;
        };
    }

    @Bean
    public AudioPlayerManager restAudioPlayerManager(Supplier<AudioPlayerManager> supplier) {
        return supplier.get();
    }

}
