package lavalink.server.config

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.IpBlock
import com.sedmelluq.discord.lavaplayer.tools.Ipv4Block
import com.sedmelluq.discord.lavaplayer.tools.Ipv6Block
import com.sedmelluq.discord.lavaplayer.tools.http.BalancingIpRoutePlanner
import com.sedmelluq.discord.lavaplayer.tools.http.RotatingIpRoutePlanner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Created by napster on 05.03.18.
 */
@Configuration
class AudioPlayerConfiguration() {

    @Bean
    fun audioPlayerManagerSupplier(sources: AudioSourcesConfig, serverConfig: ServerConfig, rateLimitConfig: RateLimitConfig, routePlannerService: RoutePlannerService) = Supplier<AudioPlayerManager> {
        val audioPlayerManager = DefaultAudioPlayerManager()

        if (serverConfig.isGcWarnings) {
            audioPlayerManager.enableGcMonitoring()
        }

        if (sources.isYoutube) {
            val youtube: YoutubeAudioSourceManager
            youtube = if (rateLimitConfig.ipBlock.isNotEmpty()) {
                val planner = routePlannerService.routePlanner()
                YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled, planner)
            } else {
                YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled)
            }
            val playlistLoadLimit = serverConfig.youtubePlaylistLoadLimit

            if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit)

            audioPlayerManager.registerSourceManager(youtube)
        }
        if (sources.isBandcamp) audioPlayerManager.registerSourceManager(BandcampAudioSourceManager())
        if (sources.isSoundcloud) audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager(serverConfig.isSoundcloudSearchEnabled))
        if (sources.isTwitch) audioPlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        if (sources.isVimeo) audioPlayerManager.registerSourceManager(VimeoAudioSourceManager())
        if (sources.isMixer) audioPlayerManager.registerSourceManager(BeamAudioSourceManager())
        if (sources.isHttp) audioPlayerManager.registerSourceManager(HttpAudioSourceManager())
        if (sources.isLocal) audioPlayerManager.registerSourceManager(LocalAudioSourceManager())

        audioPlayerManager.configuration.isFilterHotSwapEnabled = true

        audioPlayerManager
    }

    @Bean
    fun restAudioPlayerManager(supplier: Supplier<AudioPlayerManager>): AudioPlayerManager {
        return supplier.get()
    }

}
