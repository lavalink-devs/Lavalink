package lavalink.server.bootstrap

import dev.arbjerg.lavalink.protocol.v4.Version
import nl.adaptivity.xmlutil.core.KtXmlReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
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

    var classLoader = javaClass.classLoader

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

        val pluginJars = directory.listFiles()?.filter { it.extension == "jar" }
            ?.flatMap { file ->
                JarFile(file).use { jar ->
                    loadPluginManifests(jar).map { manifest -> PluginJar(manifest, file) }
                }
            }
            ?.onEach { log.info("Found plugin '${it.manifest.name}' version '${it.manifest.version}'") }
            ?: return

        val declarations = config.plugins.map { declaration ->
            if (declaration.dependency == null) throw RuntimeException("Illegal dependency declaration: null")
            val fragments = declaration.dependency!!.split(":")
            if (fragments.size != 3) throw RuntimeException("Invalid dependency \"${declaration.dependency}\"")

            val repository = declaration.repository
                ?: config.defaultPluginSnapshotRepository.takeIf { declaration.snapshot }
                ?: config.defaultPluginRepository

            Declaration(fragments[0], fragments[1], fragments[2], "${repository.removeSuffix("/")}/")
        }.distinctBy { "${it.group}:${it.name}" }

        for (declaration in declarations) {
            val jars = pluginJars.filter { it.manifest.name == declaration.name }.takeIf { it.isNotEmpty() }
                ?: pluginJars.filter { matchName(it, declaration.name) }

            var hasCurrentVersion = false

            for (jar in jars) {
                if (jar.manifest.version == declaration.version) {
                    hasCurrentVersion = true
                    // Don't clean up the jar if it's a current version.
                    continue
                }

                // Delete versions of the plugin that aren't the same as declared version.
                if (!jar.file.delete()) throw RuntimeException("Failed to delete ${jar.file.path}")
                log.info("Deleted ${jar.file.path} (new version: ${declaration.version})")

            }

            if (!hasCurrentVersion) {
                val url = declaration.url
                val file = File(directory, declaration.canonicalJarName)
                downloadJar(file, url)
            }

            checkPluginForUpdates(declaration)
        }
    }

    private fun checkPluginForUpdates(declaration: Declaration) {
        val baseSplitPath = declaration.url.split('/').dropLast(2)
        val metadataUrl = baseSplitPath.joinToString("/") + "/maven-metadata.xml"

        val url = URL(metadataUrl)
        val conn = url.openConnection() as HttpURLConnection

        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            log.warn("Failed to check for updates for ${declaration.name}: ${conn.responseMessage}")
            return
        }

        val metadata: Metadata
        conn.inputStream.use {
            metadata = xml.decodeFromReader(Metadata.serializer(), KtXmlReader(it))
        }

        val current = Version.fromSemver(declaration.version)
        var latest = metadata.versioning.latest
        if (latest.isEmpty()) {
            latest = metadata.versioning.release
        }
        if (latest.isEmpty()) {
            latest = (metadata.versioning.versions.lastOrNull() ?: "")
        }

        if (latest.isEmpty()) {
            return
        }

        val latestVersion = Version.fromSemver(latest)
        if (latestVersion > current) {
            log.warn("A newer version of '${declaration.name}' was found: '$latestVersion', The current version is '$current' please update the version in your configuration.")
        }
    }

    private fun downloadJar(output: File, url: String, isRetry: Boolean = false) {
        if (!isRetry) {
            log.info("Downloading $url")
        }

        Channels.newChannel(URL(url).openStream()).use {
            FileOutputStream(output).channel.transferFrom(it, 0, Long.MAX_VALUE)
        }

        if (output.length() == 0L) {
            if (isRetry) {
                if (!output.delete()) {
                    log.warn("Could not delete empty plugin file: ${output.path}")
                }
                throw RuntimeException("Failed to download plugin from $url")
            }

            log.warn("Downloaded plugin is empty, re-downloading...")
            if (!output.delete()) {
                throw RuntimeException("Failed to delete empty plugin file: ${output.path}")
            }
            downloadJar(output, url, true)
        }
    }

    private fun readClasspathManifests(): List<PluginManifest> {
        return PathMatchingResourcePatternResolver()
            .getResources("classpath*:lavalink-plugins/*.properties")
            .map { parsePluginManifest(it.inputStream) }
            .onEach { log.info("Found plugin '${it.name}' version '${it.version}'") }
    }

    private fun loadJars(): List<PluginManifest> {
        val directory = File(config.pluginsDir).takeIf { it.isDirectory }
            ?: return emptyList()

        val jarsToLoad = directory.listFiles()?.filter { it.isFile && it.extension == "jar" }
            ?.filter {
                if (it.length() == 0L) {
                    log.warn("Plugin jar is empty, deleting...: ${it.path}")
                    if (!it.delete()) {
                        log.error("Failed to delete empty plugin jar: ${it.path}")
                    }
                    false
                } else {
                    true
                }
            }
            ?.takeIf { it.isNotEmpty() }
            ?: return emptyList()

        classLoader = URLClassLoader.newInstance(
            jarsToLoad.map { URL("jar:file:${it.absolutePath}!/") }.toTypedArray(),
            javaClass.classLoader
        )

        return jarsToLoad.flatMap { loadJar(it, classLoader) }
    }

    private fun loadJar(file: File, cl: ClassLoader): List<PluginManifest> {
        val jar = JarFile(file)
        val manifests = loadPluginManifests(jar)
        var classCount = 0

        jar.use {
            if (manifests.isEmpty()) {
                throw RuntimeException("No plugin manifest found in ${file.path}")
            }

            val allowedPaths = manifests.map { manifest -> manifest.path.replace(".", "/") }

            for (entry in it.entries()) {
                if (entry.isDirectory ||
                    !entry.name.endsWith(".class") ||
                    allowedPaths.none(entry.name::startsWith)
                ) continue

                cl.loadClass(entry.name.dropLast(6).replace("/", "."))
                classCount++
            }
        }

        log.info("Loaded '${file.name}' ($classCount classes)")
        return manifests
    }

    private fun loadPluginManifests(jar: JarFile): List<PluginManifest> {
        return jar.entries().asSequence()
            .filter { !it.isDirectory && it.name.startsWith("lavalink-plugins/") && it.name.endsWith(".properties") }
            .map { parsePluginManifest(jar.getInputStream(it)) }
            .toList()
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

    private fun matchName(jar: PluginJar, name: String): Boolean {
        // removeSuffix removes names ending with "-v", such as -v1.0.0
        // and then the subsequent removeSuffix call removes trailing "-", which
        // usually precedes a version number, such as my-plugin-1.0.0.
        // We strip these to produce the name of the jar's file.
        val jarName = jar.file.nameWithoutExtension.takeWhile { !it.isDigit() }
            .removeSuffix("-v")
            .removeSuffix("-")

        return name == jarName
    }

    private data class PluginJar(val manifest: PluginManifest, val file: File)
    private data class Declaration(val group: String, val name: String, val version: String, val repository: String) {
        val canonicalJarName = "$name-$version.jar"
        val url = "$repository${group.replace(".", "/")}/$name/$version/$name-$version.jar"
    }
}
