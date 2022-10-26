package lavalink.server.io

import dev.arbjerg.lavalink.protocol.Plugin
import dev.arbjerg.lavalink.protocol.Plugins
import lavalink.server.bootstrap.PluginManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PluginsEndpoint(pluginManager: PluginManager) {

    private val plugins = pluginManager.pluginManifests.map {
        Plugins(listOf<Plugin>().apply { Plugin(it.name, it.version) })
    }

    @GetMapping("/plugins")
    fun plugins() = plugins

}
