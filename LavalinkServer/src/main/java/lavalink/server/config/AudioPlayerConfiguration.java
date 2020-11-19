package lavalink.server.config;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudDataReader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudFormatHandler;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudHtmlDataLoader;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.DefaultSoundCloudPlaylistLoader;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
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
                YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled());
                Integer playlistLoadLimit = serverConfig.getYoutubePlaylistLoadLimit();

                if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit);
                audioPlayerManager.registerSourceManager(youtube);
            }
            if (sources.isSoundcloud()) {
                DefaultSoundCloudDataReader dataReader = new DefaultSoundCloudDataReader();
                DefaultSoundCloudHtmlDataLoader htmlDataLoader = new DefaultSoundCloudHtmlDataLoader();
                DefaultSoundCloudFormatHandler formatHandler = new DefaultSoundCloudFormatHandler();

                audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager(
                        serverConfig.isSoundcloudSearchEnabled(),
                        dataReader,
                        htmlDataLoader,
                        formatHandler,
                        new DefaultSoundCloudPlaylistLoader(htmlDataLoader, dataReader, formatHandler)
                ));
            }
            if (sources.isBandcamp()) audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
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
