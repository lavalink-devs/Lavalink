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
import com.sedmelluq.discord.lavaplayer.tools.Ipv4Block
import com.sedmelluq.discord.lavaplayer.tools.Ipv6Block
import com.sedmelluq.discord.lavaplayer.tools.http.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * Created by napster on 05.03.18.
 */
@Configuration
class AudioPlayerConfiguration {

    @Bean
    fun audioPlayerManagerSupplier(sources: AudioSourcesConfig, serverConfig: ServerConfig, rateLimitConfig: RateLimitConfig?, routePlanner: AbstractRoutePlanner?) = Supplier<AudioPlayerManager> {
        val audioPlayerManager = DefaultAudioPlayerManager()

        if (serverConfig.isGcWarnings) {
            audioPlayerManager.enableGcMonitoring()
        }

        if (sources.isYoutube) {
            val youtube: YoutubeAudioSourceManager = if (rateLimitConfig != null && rateLimitConfig.ipBlock.isNotEmpty() && routePlanner != null) {
                YoutubeAudioSourceManager(serverConfig.isYoutubeSearchEnabled, routePlanner)
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

    @Bean
    fun routePlanner(rateLimitConfig: RateLimitConfig?): AbstractRoutePlanner? {
        if (rateLimitConfig?.ipBlock?.isNotEmpty() != true) return null
        val blacklisted = rateLimitConfig.excludedIps.map { InetAddress.getByName(it) }
        val filter = Predicate<InetAddress> {
            !blacklisted.contains(it)
        }
        val ipBlock = when {
            Ipv4Block.isIpv4CidrBlock(rateLimitConfig.ipBlock) -> Ipv4Block(rateLimitConfig.ipBlock)
            Ipv6Block.isIpv6CidrBlock(rateLimitConfig.ipBlock) -> Ipv6Block(rateLimitConfig.ipBlock)
            else -> throw RuntimeException("Invalid IP Block, make sure to provide a valid CIDR notation")
        }
        val strategy = rateLimitConfig.strategy.toLowerCase().trim()
        return when {
            strategy == "rotateonban" -> RotatingIpRoutePlanner(ipBlock, filter, rateLimitConfig.searchTriggersFail)
            strategy == "loadbalance" -> BalancingIpRoutePlanner(ipBlock, filter, rateLimitConfig.searchTriggersFail)
            strategy == "nanoswitch" -> NanoIpRoutePlanner(ipBlock, rateLimitConfig.searchTriggersFail)
            strategy == "rotatingnanoswitch" -> RotatingNanoIpRoutePlanner(ipBlock, filter, rateLimitConfig.searchTriggersFail)
            else -> throw RuntimeException("Invalid strategy, only RotateOnBan, LoadBalance and NanoSwitch can be used")
        }
    }

}
