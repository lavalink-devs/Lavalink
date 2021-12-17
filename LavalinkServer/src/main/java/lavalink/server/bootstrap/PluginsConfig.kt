package lavalink.server.bootstrap

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "lavalink")
@Component
class PluginsConfig {
    var plugins: List<PluginDeclaration> = emptyList()
}

data class PluginDeclaration(
    var dependency: String? = null,
    var repository: String? = null
)