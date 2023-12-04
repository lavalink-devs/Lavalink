package lavalink.server.bootstrap

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.nio.channels.Channels
import java.util.*
import java.util.jar.JarFile

@SpringBootApplication
class PluginManager(val config: PluginsConfig) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PluginManager::class.java)
    }

    final val pluginManifests: MutableList<PluginManifest> = mutableListOf()
    var classLoader: ClassLoader = PluginManager::class.java.classLoader

    init {
        manageDownloads()
        pluginManifests.apply {
            addAll(readClasspathManifests())
            addAll(loadJars())
        }
    }

    private fun manageDownloads() {
        if (config.plugins.isEmpty()) return
        val directory = File(config.pluginsDir)
        directory.mkdir()

        data class PluginJar(val manifest: PluginManifest, val file: File)

        val pluginJars = directory.listFiles()!!.filter { it.extension == "jar" }.map {
            loadPluginManifests(JarFile(it)).map { manifest -> PluginJar(manifest, it) }
        }.flatten()

        data class Declaration(val group: String, val name: String, val version: String, val repository: String)

        val declarations = config.plugins.map { declaration ->
            if (declaration.dependency == null) throw RuntimeException("Illegal dependency declaration: null")
            val fragments = declaration.dependency!!.split(":")
            if (fragments.size != 3) throw RuntimeException("Invalid dependency \"${declaration.dependency}\"")

            var repository = declaration.repository
                ?: if (declaration.snapshot) config.defaultPluginSnapshotRepository else config.defaultPluginRepository
            repository = if (repository.endsWith("/")) repository else "$repository/"
            Declaration(fragments[0], fragments[1], fragments[2], repository)
        }.distinctBy { "${it.group}:${it.name}" }

        declarations.forEach declarationLoop@{ declaration ->
            var hasVersion = false
            pluginJars.forEach pluginLoop@{ jar ->
                if (declaration.version == jar.manifest.version && !hasVersion) {
                    hasVersion = true
                    // We already have this jar so don't redownload it
                    return@pluginLoop
                }

                // Delete jar of different versions
                if (!jar.file.delete()) throw RuntimeException("Failed to delete ${jar.file.path}")
                log.info("Deleted ${jar.file.path}")
            }
            if (hasVersion) return@declarationLoop

            val url = declaration.run { "$repository${group.replace(".", "/")}/$name/$version/$name-$version.jar" }
            val file = File(directory, declaration.run { "$name-$version.jar" })
            downloadJar(file, url)
        }
    }

    private fun downloadJar(output: File, url: String) {
        log.info("Downloading $url")
        Channels.newChannel(URL(url).openStream()).use {
            FileOutputStream(output).channel.transferFrom(it, 0, Long.MAX_VALUE)
        }
    }

    private fun readClasspathManifests(): List<PluginManifest> {
        return PathMatchingResourcePatternResolver()
            .getResources("classpath*:lavalink-plugins/*.properties")
            .map map@{ r ->
                val manifest = parsePluginManifest(r.inputStream)
                log.info("Found plugin '${manifest.name}' version ${manifest.version}")
                return@map manifest
            }
    }

    private fun loadJars(): List<PluginManifest> {
        val directory = File(config.pluginsDir)
        if (!directory.isDirectory) return emptyList()
        val jarsToLoad = mutableListOf<File>()

        directory.listFiles()?.forEach { file ->
            if (!file.isFile) return@forEach
            if (file.extension != "jar") return@forEach
            jarsToLoad.add(file)
        }

        if (jarsToLoad.isEmpty()) return emptyList()

        val cl = URLClassLoader.newInstance(
            jarsToLoad.map { URL("jar:file:${it.absolutePath}!/") }.toTypedArray(),
            javaClass.classLoader
        )
        classLoader = cl

        val manifests = mutableListOf<PluginManifest>()

        jarsToLoad.forEach { file ->
            try {
                manifests.addAll(loadJar(file, cl))
            } catch (e: Exception) {
                throw RuntimeException("Error loading $file", e)
            }
        }


        return manifests
    }

    private fun loadJar(file: File, cl: URLClassLoader): List<PluginManifest> {
        var classCount = 0
        val jar = JarFile(file)

        val manifests = loadPluginManifests(jar)
        if (manifests.isEmpty()) {
            throw RuntimeException("No plugin manifest found in ${file.path}")
        }
        val allowedPaths = manifests.map { it.path.replace(".", "/") }

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

    private fun loadPluginManifests(jar: JarFile): List<PluginManifest> {
        val manifests = mutableListOf<PluginManifest>()

        jar.entries().asIterator().forEach { entry ->
            if (entry.isDirectory) return@forEach
            if (!entry.name.startsWith("lavalink-plugins/")) return@forEach
            if (!entry.name.endsWith(".properties")) return@forEach

            val manifest = parsePluginManifest(jar.getInputStream(entry))
            log.info("Found plugin '${manifest.name}' version ${manifest.version}")
            manifests.add(manifest)
        }
        return manifests
    }

    private fun parsePluginManifest(stream: InputStream): PluginManifest {
        val props = stream.use {
            Properties().apply { load(it) }
        }

        val name = props.getProperty("name") ?: throw RuntimeException("Manifest is missing 'name'")
        val path = props.getProperty("path") ?: throw RuntimeException("Manifest is missing 'path'")
        val version = props.getProperty("version") ?: throw RuntimeException("Manifest is missing 'version'")
        return PluginManifest(name, path, version)
    }
}