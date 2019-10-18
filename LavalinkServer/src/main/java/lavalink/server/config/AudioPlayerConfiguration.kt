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
import com.sedmelluq.discord.lavaplayer.tools.Ipv6Block
import com.sedmelluq.discord.lavaplayer.tools.http.BalancingIpv6RoutePlanner
import com.sedmelluq.discord.lavaplayer.tools.http.RotatingIpv6RoutePlanner
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.net.Inet6Address
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Created by napster on 05.03.18.
 */
@Component
class AudioPlayerConfiguration {

    @Bean
    fun audioPlayerManagerSupplier(sources: AudioSourcesConfig, serverConfig: ServerConfig) = Supplier<AudioPlayerManager> {
        val audioPlayerManager = DefaultAudioPlayerManager()

        if (serverConfig.isGcWarnings) {
            audioPlayerManager.enableGcMonitoring()
        }

        if (sources.isYoutube) {
            val youtube: YoutubeAudioSourceManager
            youtube = if (serverConfig.balancingBlock.isNotEmpty() && !serverConfig.rotateOnBan) {
                val blacklisted = serverConfig.excludedIps.map { Inet6Address.getByName(it) }
                val filter = Predicate<Inet6Address> {
                    !blacklisted.contains(it)
                }
                val planner = if(serverConfig.rotateOnBan) {
                    RotatingIpv6RoutePlanner(Ipv6Block(serverConfig.balancingBlock), filter)
                } else {
                    BalancingIpv6RoutePlanner(Ipv6Block(serverConfig.balancingBlock), filter)
                }

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
