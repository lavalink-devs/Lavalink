package lavalink.server.config

import com.sedmelluq.discord.lavaplayer.container.MediaContainerProbe
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.*
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.*
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv4Block
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

/**
 * Created by napster on 05.03.18.
 */
@Configuration
class AudioPlayerConfiguration {

    private val log = LoggerFactory.getLogger(AudioPlayerConfiguration::class.java)

    @Bean
    fun audioPlayerManagerSupplier(
        sources: AudioSourcesConfig,
        serverConfig: ServerConfig,
        routePlanner: AbstractRoutePlanner?,
        audioSourceManagers: Collection<AudioSourceManager>,
        audioPlayerManagerConfigurations: Collection<AudioPlayerManagerConfiguration>,
        mediaContainerProbes: Collection<MediaContainerProbe>
    ): AudioPlayerManager {
        serverConfig.timeouts?.let {
            HttpClientTools.setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(it.connectTimeoutMs)
                    .setConnectionRequestTimeout(it.connectionRequestTimeoutMs)
                    .setSocketTimeout(it.socketTimeoutMs)
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build()
            )

            HttpClientTools.setNoCookiesRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(it.connectTimeoutMs)
                    .setConnectionRequestTimeout(it.connectionRequestTimeoutMs)
                    .setSocketTimeout(it.socketTimeoutMs)
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .build()
            )
        }

        val audioPlayerManager = DefaultAudioPlayerManager()

        if (serverConfig.isGcWarnings) {
            audioPlayerManager.enableGcMonitoring()
        }

        if (serverConfig.isNonAllocatingFrameBuffer) {
            log.info("Using a non-allocating frame buffer")
            audioPlayerManager.configuration.setFrameBufferFactory(::NonAllocatingAudioFrameBuffer)
        }

        val defaultFrameBufferDuration = audioPlayerManager.frameBufferDuration
        serverConfig.frameBufferDurationMs?.let {
            if (it < 200) { // At the time of writing, LP enforces a minimum of 200ms.
                log.warn("Buffer size of ${it}ms is illegal. Defaulting to ${defaultFrameBufferDuration}ms")
            }

            val bufferDuration = it.takeIf { it >= 200 } ?: defaultFrameBufferDuration
            log.debug("Setting frame buffer duration to ${bufferDuration}ms")
            audioPlayerManager.frameBufferDuration = bufferDuration
        }

        val defaultOpusEncodingQuality = AudioConfiguration.OPUS_QUALITY_MAX
        audioPlayerManager.configuration.let {
            serverConfig.opusEncodingQuality?.let { opusQuality ->
                if (opusQuality !in 0..10) {
                    log.warn("Opus encoding quality $opusQuality is not within the range of 0 to 10. Defaulting to $defaultOpusEncodingQuality")
                }

                val qualitySetting = opusQuality.takeIf { it in 0..10 } ?: defaultOpusEncodingQuality
                log.debug("Setting opusEncodingQuality to $qualitySetting")
                it.opusEncodingQuality = qualitySetting
            }

            serverConfig.resamplingQuality?.let { resamplingQuality ->
                log.debug("Setting resamplingQuality to $resamplingQuality")
                it.resamplingQuality = resamplingQuality
            }
        }

        val defaultTrackStuckThresholdMs = TimeUnit.NANOSECONDS.toMillis(audioPlayerManager.trackStuckThresholdNanos)
        serverConfig.trackStuckThresholdMs?.let {
            if (it < 100) {
                log.warn("Track Stuck Threshold of ${it}ms is too small. Defaulting to ${defaultTrackStuckThresholdMs}ms")
            }

            val trackStuckThresholdMs: Long = it.takeIf { it >= 100 } ?: defaultTrackStuckThresholdMs
            log.debug("Setting Track Stuck Threshold to ${trackStuckThresholdMs}ms")
            audioPlayerManager.setTrackStuckThreshold(trackStuckThresholdMs)
        }

        serverConfig.useSeekGhosting?.let { seekGhosting ->
            log.debug("Setting useSeekGhosting to $seekGhosting")
            audioPlayerManager.setUseSeekGhosting(seekGhosting)
        }

        val mcr: MediaContainerRegistry = MediaContainerRegistry.extended(*mediaContainerProbes.toTypedArray())

        if (sources.isYoutube) {
            log.warn(
                """
                    The default Youtube source is now deprecated and won't receive further updates.
                    You should use the new Youtube source plugin instead.
                    https://github.com/lavalink-devs/youtube-source#plugin.
                    To disable this warning, set 'lavalink.server.sources.youtube' to false in your application.yml.
                """.trimIndent()
            )
            val youtubeConfig = serverConfig.youtubeConfig
            val youtube: YoutubeAudioSourceManager
            if (youtubeConfig != null) {
                if (youtubeConfig.email.isBlank() && youtubeConfig.password.isBlank()) {
                    log.info("Email and password fields are blank, some age restricted videos will throw exceptions")
                }
                youtube = YoutubeAudioSourceManager(
                    serverConfig.isYoutubeSearchEnabled,
                    youtubeConfig.email,
                    youtubeConfig.password
                )
            } else {
                youtube = YoutubeAudioSourceManager(
                    serverConfig.isYoutubeSearchEnabled,
                    "",
                    ""
                )
                log.debug("Youtube config block is not found")
            }
            if (routePlanner != null) {
                val retryLimit = serverConfig.ratelimit?.retryLimit ?: -1
                when {
                    retryLimit < 0 -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube).setup()
                    retryLimit == 0 -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube)
                        .withRetryLimit(Int.MAX_VALUE).setup()

                    else -> YoutubeIpRotatorSetup(routePlanner).forSource(youtube).withRetryLimit(retryLimit).setup()

                }
            }
            val playlistLoadLimit = serverConfig.youtubePlaylistLoadLimit
            if (playlistLoadLimit != null) youtube.setPlaylistPageCount(playlistLoadLimit)

            audioPlayerManager.registerSourceManager(youtube)
        }
        if (sources.isSoundcloud) {
            val dataReader = DefaultSoundCloudDataReader()
            val dataLoader = DefaultSoundCloudDataLoader()
            val formatHandler = DefaultSoundCloudFormatHandler()

            audioPlayerManager.registerSourceManager(
                SoundCloudAudioSourceManager(
                    serverConfig.isSoundcloudSearchEnabled,
                    dataReader,
                    dataLoader,
                    formatHandler,
                    DefaultSoundCloudPlaylistLoader(dataLoader, dataReader, formatHandler)
                )
            )
        }
        if (sources.isBandcamp) audioPlayerManager.registerSourceManager(BandcampAudioSourceManager())
        if (sources.isTwitch) audioPlayerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        if (sources.isVimeo) audioPlayerManager.registerSourceManager(VimeoAudioSourceManager())
        if (sources.isNico) audioPlayerManager.registerSourceManager(NicoAudioSourceManager())
        if (sources.isLocal) audioPlayerManager.registerSourceManager(LocalAudioSourceManager(mcr))

        audioSourceManagers.forEach {
            audioPlayerManager.registerSourceManager(it)
            log.info("Registered $it provided from a plugin")
        }

        audioPlayerManager.configuration.isFilterHotSwapEnabled = true


        val am = audioPlayerManagerConfigurations
            .fold(audioPlayerManager as AudioPlayerManager) { player, plugin ->
                plugin.configure(player)
            }

        // This must be loaded last
        if (sources.isHttp) {
            val httpAudioSourceManager = HttpAudioSourceManager(mcr)

            serverConfig.httpConfig?.let { httpConfig ->
                httpAudioSourceManager.configureBuilder {
                    if (httpConfig.proxyHost.isNotBlank()) {
                        val credsProvider: CredentialsProvider = BasicCredentialsProvider()
                        credsProvider.setCredentials(
                            AuthScope(httpConfig.proxyHost, httpConfig.proxyPort),
                            UsernamePasswordCredentials(httpConfig.proxyUser, httpConfig.proxyPassword)
                        )

                        it.setProxy(HttpHost(httpConfig.proxyHost, httpConfig.proxyPort))
                        if (httpConfig.proxyUser.isNotBlank()) {
                            it.setDefaultCredentialsProvider(credsProvider)
                        }
                    }
                }
            }

            audioPlayerManager.registerSourceManager(httpAudioSourceManager)
        }

        return am
    }

    @Bean
    fun routePlanner(serverConfig: ServerConfig): AbstractRoutePlanner? {
        val rateLimitConfig = serverConfig.ratelimit
        if (rateLimitConfig == null) {
            log.debug("No rate limit config block found, skipping setup of route planner")
            return null
        }
        val ipBlockList = rateLimitConfig.ipBlocks
        if (ipBlockList.isEmpty()) {
            log.info("List of ip blocks is empty, skipping setup of route planner")
            return null
        }

        val blacklisted = rateLimitConfig.excludedIps.map { InetAddress.getByName(it) }
        val filter = Predicate<InetAddress> {
            !blacklisted.contains(it)
        }
        val ipBlocks = ipBlockList.map {
            when {
                Ipv4Block.isIpv4CidrBlock(it) -> Ipv4Block(it)
                Ipv6Block.isIpv6CidrBlock(it) -> Ipv6Block(it)
                else -> throw RuntimeException("Invalid IP Block '$it', make sure to provide a valid CIDR notation")
            }
        }

        return when (rateLimitConfig.strategy.lowercase(Locale.getDefault()).trim()) {
            "rotateonban" -> RotatingIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            "loadbalance" -> BalancingIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            "nanoswitch" -> NanoIpRoutePlanner(ipBlocks, rateLimitConfig.searchTriggersFail)
            "rotatingnanoswitch" -> RotatingNanoIpRoutePlanner(ipBlocks, filter, rateLimitConfig.searchTriggersFail)
            else -> throw RuntimeException("Unknown strategy!")
        }
    }

}
