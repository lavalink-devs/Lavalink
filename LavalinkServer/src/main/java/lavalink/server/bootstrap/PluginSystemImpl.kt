package lavalink.server.bootstrap

import dev.arbjerg.lavalink.api.PluginSystem
import lavalink.server.info.AppInfo
import org.pf4j.spring.ExtensionsInjector
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

@Import(AppInfo::class)
@SpringBootApplication
class PluginSystemImpl(
    val config: PluginsConfig,
    override val manager: PluginLoader,
) : PluginSystem {
    val httpClient = HttpClient.newHttpClient()

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PluginSystemImpl::class.java)
    }

    init {
        manager.loadPlugins()
        manageDownloads()
    }

    @OptIn(ExperimentalPathApi::class)
    private fun manageDownloads() {
        if (config.plugins.isEmpty()) return

        val directory = Path(config.pluginsDir)
        directory.createDirectories()

        val declarations = config.plugins.map { declaration ->
            if (declaration.dependency == null) throw RuntimeException("Illegal dependency declaration: null")
            val fragments = declaration.dependency!!.split(":")
            if (fragments.size != 3) throw RuntimeException("Invalid dependency \"${declaration.dependency}\"")

            val repository = declaration.repository
                ?: config.defaultPluginSnapshotRepository.takeIf { declaration.snapshot }
                ?: config.defaultPluginRepository

            Declaration(fragments[0], fragments[1], fragments[2], "${repository.removeSuffix("/")}/")
        }.distinctBy { "${it.group}:${it.name}" }

        val pluginManifests = manager.plugins.map { it.descriptor as LavalinkPluginDescriptor }

        for (declaration in declarations) {
            val manifest = pluginManifests.firstOrNull { it.pluginId == declaration.name }

            if (manifest?.version != declaration.version) {
                if (manifest != null) {
                    manager.deletePlugin(manifest.pluginId)
                }

                val url = declaration.url
                val file = directory / declaration.canonicalJarName
                if (downloadJar(file, url)) {
                    manager.loadPlugin(file)
                }
            }
        }
    }

    private fun downloadJar(output: Path, url: String): Boolean {
        log.info("Downloading {}", url)

        val request = HttpRequest.newBuilder(URI(url)).build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(output))
        if (response.statusCode() != 200) {
            log.warn("Could not download {}, got unexpected status code {}", url, response.statusCode())
            return false
        }
        return response.statusCode() == 200
    }

    private data class Declaration(val group: String, val name: String, val version: String, val repository: String) {
        val canonicalJarName = "$name-$version.jar"
        val url = "$repository${group.replace(".", "/")}/$name/$version/$name-$version.jar"
    }
}
