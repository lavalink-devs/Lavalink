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
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by napster on 05.03.18.
 */
@Component
public class AudioPlayerConfiguration {

    @Bean
    public AudioPlayerManager audioPlayerManager(AudioSourcesConfig sources, ServerConfig serverConfig) {

        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.enableGcMonitoring();

        if (sources.isYoutube()) {
            YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();
            Integer playlistLoadLimit = serverConfig.getYoutubePlaylistLoadLimit();

            if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit);
            audioPlayerManager.registerSourceManager(youtube);
        }
        if (sources.isBandcamp()) audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        if (sources.isSoundcloud()) audioPlayerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        if (sources.isTwitch()) audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        if (sources.isVimeo()) audioPlayerManager.registerSourceManager(new VimeoAudioSourceManager());
        if (sources.isMixer()) audioPlayerManager.registerSourceManager(new BeamAudioSourceManager());
        if (sources.isHttp()) audioPlayerManager.registerSourceManager(new HttpAudioSourceManager());
        if (sources.isLocal()) audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());

        return audioPlayerManager;
    }

}
