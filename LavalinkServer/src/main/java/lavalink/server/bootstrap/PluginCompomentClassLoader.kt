package lavalink.server.bootstrap

import org.pf4j.PluginWrapper
import org.pf4j.util.FileUtils
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.Enumeration
import java.util.stream.Stream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

private val LOG = LoggerFactory.getLogger(PluginComponentClassLoader::class.java)

/**
 * Implementation of [ClassLoader] which is aware of separate plugin class loaders.
 */
class PluginComponentClassLoader(pluginLoader: PluginLoader) : ClassLoader() {

    // This list does not necessarily need to be up to date
    // it just acts as a fast path to resolve these names more quickly
    private val systemPackages = listOf(
        "java",
        "javax",
        "lavalink",
        "kotlin",
        "kotlinx",
        "jarkarte",
        "git",
        "org/springframework",
        "org/jetbrains/kotlin",
        "org/pf4j",
        "com/sedmelluq",
        "dev/arbjerg",
        "io/prometheus",
        "club/minnced",
        "ch/qos/logback",
        "io/sentry",
        "com/github/oshi",
        "freemarker",
        "com/samskivert",
        "groovy",
        "org/thymeleaf/spring6",
        "org/apache/jasper",
        "org/aspectj",
        "com/fasterxml/jackson/",
        "com/hazelcast",
        "com/couchbase",
        "org/infinispan/spring",
        "org/cache2k",
        "com/github/benmanes/caffeine/cache",
        "com/fasterxml/jackson/dataformat/xml",
        "io/r2dbc/spi",
        "reactor",
        "org/eclipse/jetty",
        "org/apache/catalina",
        "org/apache/coyote",
        "freemarker/template",
        "com/samskivert/mustache",
        "org/thymeleaf/spring6",
        "org/apache/jasper/compiler",
        "com/google/gson",
        "META-INF"
    )

    private val cache = pluginLoader.plugins
        .filter { (it.descriptor as LavalinkPluginDescriptor).manifestVersion == PluginDescriptor.Version.V1 }
        .associateBy { (it.descriptor as LavalinkPluginDescriptor).path.replace('.', '/') }
        .toMutableMap()

    // Due to the nature of the legacy class loading some plugins might produce packages outside their defined paths
    // For this reason we need to build a map of those packages
    private val legacyCache = pluginLoader.plugins
        .asSequence()
        .filter { (it.descriptor as LavalinkPluginDescriptor).manifestVersion == PluginDescriptor.Version.V1 }
        .map { it to findClassesProvidedByPlugin(it.pluginPath) }
        .toList()

    fun findClassesProvidedByPlugin(pluginPath: Path, vararg path: String): List<Path> {
        val fixPath = FileUtils.getPath(pluginPath, "", *path)
        val childPaths = fixPath.listDirectoryEntries()
            .filter { it.isDirectory() }
        return (childPaths + childPaths
            .flatMap {
                findClassesProvidedByPlugin(pluginPath, *path, it.fileName.toString())
            })
            // Filter out top-level directories
            .filter { it.toString().contains('/') }
    }


    private fun findLegacyPackage(name: String): PluginWrapper? {
        return legacyCache.firstOrNull { (_, classes) ->
            classes.any { name.startsWith(it.toString()) }
        }?.first ?: run {
            val newPath = name.substringBeforeLast('/')
            if (newPath == name) return null
            findLegacyPackage(newPath)
        }
    }

    private fun findPackage(pack: String): PluginWrapper? {
        return cache.toList().find { (key, _) ->
            pack.startsWith(key)
        }?.second ?: run {
            val newPath = pack.substringBeforeLast('/')
            if (newPath == pack) return null
            findPackage(newPath)
        }
    }

    private fun findClassLoader(name: String?): ClassLoader? {
        if (name == null) return null
        // Sometimes .class files are requested, as paths
        // other times the class name gets requested
        val pack = if (name.endsWith(".class")) {
            name.substringBeforeLast('/')
        } else {
            name.substringBeforeLast('.').replace('.', '/')
        }
        if (systemPackages.any { pack.startsWith(it) }) return javaClass.classLoader

        LOG.debug("Found package of {} to be {}", name, pack)
        val plugin = cache.getOrPut(pack) { findPackage(pack) } ?: findLegacyPackage(name)
        ?: return javaClass.classLoader

        LOG.debug("Found class {} to be provided by plugin {}", name, plugin.pluginId)

        return plugin.pluginClassLoader
    }

    override fun loadClass(name: String?): Class<*>? = findClassLoader(name)?.loadClass(name)
    override fun getResource(name: String?): URL? = findClassLoader(name)?.getResource(name)
    override fun getResources(name: String?): Enumeration<URL?>? = findClassLoader(name)?.getResources(name)
    override fun getResourceAsStream(name: String?): InputStream? = findClassLoader(name)?.getResourceAsStream(name)
    override fun resources(name: String?): Stream<URL?>? = findClassLoader(name)?.resources(name)
}