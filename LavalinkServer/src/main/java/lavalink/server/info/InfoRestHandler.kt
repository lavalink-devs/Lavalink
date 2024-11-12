package lavalink.server.info

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.v4.*
import lavalink.server.bootstrap.PluginSystemImpl
import lavalink.server.config.ServerConfig
import org.pf4j.Extension
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by napster on 08.03.19.
 */

@Extension(ordinal = Int.MAX_VALUE) // Register this last, as we need to load plugin configuration contributors first
@RestController
class InfoRestHandler(
    appInfo: AppInfo,
    gitRepoState: GitRepoState,
    audioPlayerManager: AudioPlayerManager,
    pluginManager: PluginSystemImpl,
    serverConfig: ServerConfig,
    filterExtensions: List<AudioFilterExtension>
) {

    private val enabledFilers = (listOf(
        "volume",
        "equalizer",
        "karaoke",
        "timescale",
        "tremolo",
        "vibrato",
        "distortion",
        "rotation",
        "channelMix",
        "lowPass"
    ) + filterExtensions.map { it.name }).filter {
        it !in serverConfig.filters || serverConfig.filters[it] == true
    }

    private val info = Info(
        Version.fromSemver(appInfo.versionBuild),
        appInfo.buildTime,
        Git(gitRepoState.branch, gitRepoState.commitIdAbbrev, gitRepoState.commitTime * 1000),
        System.getProperty("java.version"),
        PlayerLibrary.VERSION,
        audioPlayerManager.sourceManagers.map { it.sourceName },
        enabledFilers,
        Plugins(pluginManager.manager.plugins.map {
            val descriptor = it.descriptor
            Plugin(descriptor.pluginId, descriptor.version)
        })
    )
    private val version = appInfo.versionBuild

    @GetMapping("/v4/info")
    fun info() = info

    @GetMapping("/version")
    fun version() = version
}
