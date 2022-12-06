package lavalink.server.io

import dev.arbjerg.lavalink.protocol.v3.Plugin
import dev.arbjerg.lavalink.protocol.v3.Plugins
import lavalink.server.bootstrap.PluginManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PluginsEndpoint(pluginManager: PluginManager) {

    private val plugins = Plugins(pluginManager.pluginManifests.map { Plugin(it.name, it.version) })

    @GetMapping("/plugins")
    fun plugins() = plugins

}
