package lavalink.server.io

import lavalink.server.bootstrap.PluginManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PluginsEndpoint {

    private val data = PluginManager.pluginManifests.map {
        mutableMapOf<Any, Any>().apply {
            put("name", it.name)
            put("version", it.version)
        }
    }

    @GetMapping("/plugins")
    fun plugins() = data

}