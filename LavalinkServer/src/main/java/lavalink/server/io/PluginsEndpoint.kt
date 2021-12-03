package lavalink.server.io

import lavalink.api.PluginInfo
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PluginsEndpoint(plugins: Collection<PluginInfo>) {

    private val data = plugins.map {
        mutableMapOf<Any, Any>().apply {
            putAll(it.supplementalData)
            put("name", it.name)
            put("version", "${it.major}.${it.minor}.${it.patch}")
        }
    }

    @GetMapping("/plugins")
    fun plugins() = data

}