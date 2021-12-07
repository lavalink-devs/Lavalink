package lavalink.server.bootstrap

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.util.*
import java.util.jar.JarFile


@SpringBootApplication
class PluginManager(config: PluginsConfig) {
    init {
        manageDownloads(config)
        pluginManifests = mutableListOf<PluginManifest>().apply {
            addAll(readClasspathManifests())
            addAll(loadJars())
        }
    }

    private fun manageDownloads(config: PluginsConfig) {
        // todo
    }

    private fun readClasspathManifests(): List<PluginManifest> {
        return PathMatchingResourcePatternResolver()
            .getResources("classpath*:lavalink-plugins/*.properties")
            .map { r -> parsePluginManifest(r.inputStream) }
    }

    private fun loadJars(): List<PluginManifest> {
        val directory = File("./plugins")
        if (!directory.isDirectory) return emptyList()

        val manifests = mutableListOf<PluginManifest>()

        Files.list(File("./plugins").toPath()).forEach { path ->
            val file = path.toFile()
            if (!file.isFile) return@forEach
            if (file.extension != "jar") return@forEach
            try {
                manifests.addAll(loadJar(file))
            } catch (e: Exception) {
                throw RuntimeException("Error loading $file", e)
            }
        }

        return manifests
    }

    private fun loadJar(file: File): MutableList<PluginManifest> {
        val cl = URLClassLoader.newInstance(arrayOf(URL("jar:" + file.absolutePath + "!/")))
        var classCount = 0
        val jar = JarFile(file)
        val manifests = mutableListOf<PluginManifest>()

        jar.entries().asIterator().forEach { entry ->
            if (entry.isDirectory) return@forEach
            if (!entry.name.endsWith(".class")) return@forEach
            if (!entry.name.startsWith("lavalink-plugins/")) return@forEach
            if (!entry.name.endsWith(".properties")) return@forEach
            manifests.add(parsePluginManifest(jar.getInputStream(entry)))
        }

        if (manifests.isEmpty()) {
            throw RuntimeException("No plugin manifest found in ${file.path}")
        }
        val allowedPaths = manifests.map { it.path }

        jar.entries().asIterator().forEach { entry ->
            if (entry.isDirectory) return@forEach
            if (!entry.name.endsWith(".class")) return@forEach
            if (!allowedPaths.any { entry.name.startsWith(it) }) return@forEach
            cl.loadClass(entry.name.dropLast(6).replace("/", "."))
            classCount++
        }
        log.info("Loaded ${file.name} ($classCount classes)")
        return manifests
    }

    private fun parsePluginManifest(stream: InputStream): PluginManifest {
        val props = stream.use {
            Properties().apply { load(it) }
        }

        val name = props.getProperty("name") ?: throw RuntimeException("Manifest is missing 'name'")
        val path = props.getProperty("path") ?: throw RuntimeException("Manifest is missing 'path'")
        val version = props.getProperty("version") ?: throw RuntimeException("Manifest is missing 'version'")
        log.info("Found $name version $version")
        return PluginManifest(name, path, version)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PluginManager::class.java)
        private lateinit var pluginManifests: List<PluginManifest>
    }
}