package lavalink.server.bootstrap

import org.pf4j.DefaultPluginDescriptor
import org.pf4j.PropertiesPluginDescriptorFinder
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.inputStream
import kotlin.io.path.useDirectoryEntries
import org.pf4j.PluginDescriptor as BasePluginDescriptor

interface PluginDescriptor : BasePluginDescriptor {
    val path: String
    val manifestVersion: Version
    val springConfigurationFiles: List<String>
    override fun getVersion(): String
    override fun getPluginId(): String
    override fun getPluginClass(): Nothing? = null

    enum class Version {
        /**
         * Legacy version.
         */
        V1,

        /**
         * Current up-to-date version.
         */
        V2
    }
}

class LavalinkPluginDescriptor(override val manifestVersion: PluginDescriptor.Version) : DefaultPluginDescriptor(),
    PluginDescriptor {
    override lateinit var path: String
    override lateinit var springConfigurationFiles: List<String>
    override fun getPluginClass(): Nothing? = super<PluginDescriptor>.getPluginClass()
    override fun setPluginClass(pluginClassName: String?): BasePluginDescriptor = this
    public override fun setPluginVersion(version: String): DefaultPluginDescriptor = super.setPluginVersion(version)
}

object LavalinkDescriptorFinder : PropertiesPluginDescriptorFinder() {
    override fun createPluginDescriptorInstance(): LavalinkPluginDescriptor =
        LavalinkPluginDescriptor(PluginDescriptor.Version.V2)

    override fun createPluginDescriptor(properties: Properties): BasePluginDescriptor {
        return super.createPluginDescriptor(properties).apply {
            val configurations = properties.getProperty("plugin.configurations")
            (this as LavalinkPluginDescriptor).springConfigurationFiles = if (configurations != null) {
                configurations.split(",\\s*".toRegex())
            } else {
                emptyList()
            }
        }
    }
}

object LegacyLavalinkDescriptorFinder : PropertiesPluginDescriptorFinder() {
    override fun readProperties(pluginPath: Path): Properties {
        val descriptorDirectory = getPropertiesPath(pluginPath, "lavalink-plugins")
        val descriptor = descriptorDirectory.useDirectoryEntries("*.properties") { it.singleOrNull() }
            ?: error("Found more than one descriptor in $descriptorDirectory")

        val properties = Properties()
        descriptor.inputStream().use(properties::load)

        return properties
    }

    override fun createPluginDescriptorInstance(): LavalinkPluginDescriptor =
        LavalinkPluginDescriptor(PluginDescriptor.Version.V1)

    override fun createPluginDescriptor(properties: Properties): BasePluginDescriptor =
        createPluginDescriptorInstance().apply {
            path = properties.getProperty("path") ?: error("'path' is not specified in plugin properties")
            pluginId = properties.getProperty("name") ?: error("'name' is not specified in plugin properties")
            setPluginVersion(
                properties.getProperty("version") ?: error("'version' is not specified in plugin properties")
            )
        }
}
