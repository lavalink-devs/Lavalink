package lavalink.server.bootstrap

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "lavalink")
@Component
class PluginsConfig {
    var plugins: List<PluginDeclaration> = emptyList()
    var pluginsDir: String = "./plugins"
    var developmentMode: Boolean = false
    var defaultPluginRepository: String = "https://maven.lavalink.dev/releases"
    var defaultPluginSnapshotRepository: String = "https://maven.lavalink.dev/snapshots"
}

data class PluginDeclaration(
    var dependency: String? = null,
    var repository: String? = null,
    var snapshot: Boolean = false
)