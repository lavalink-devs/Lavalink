package lavalink.server.v3

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.v3.*
import lavalink.server.bootstrap.PluginManager
import lavalink.server.config.ServerConfig
import lavalink.server.info.AppInfo
import lavalink.server.info.GitRepoState
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by napster on 08.03.19.
 */
@RestController
class InfoRestHandlerV3(
    appInfo: AppInfo,
    gitRepoState: GitRepoState,
    audioPlayerManager: AudioPlayerManager,
    pluginManager: PluginManager,
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
        Plugins(pluginManager.pluginManifests.map {
            Plugin(it.name, it.version)
        })
    )

    @GetMapping("/v3/info")
    fun info() = info

}
