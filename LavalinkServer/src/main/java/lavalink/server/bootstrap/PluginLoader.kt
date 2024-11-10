package lavalink.server.bootstrap

import lavalink.server.info.AppInfo
import org.pf4j.ClassLoadingStrategy
import org.pf4j.CompoundPluginDescriptorFinder
import org.pf4j.CompoundPluginLoader
import org.pf4j.DefaultPluginLoader
import org.pf4j.DefaultPluginManager
import org.pf4j.DevelopmentPluginLoader
import org.pf4j.PluginLoader as BasePluginLoader
import org.pf4j.PluginClassLoader
import org.pf4j.PluginDescriptor
import org.pf4j.PluginDescriptorFinder
import org.pf4j.PluginFactory
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.util.jar.Manifest
import kotlin.io.path.Path
import kotlin.io.path.extension

@Component
class PluginLoader(pluginsConfig: PluginsConfig, private val appInfo: AppInfo) : DefaultPluginManager(Path(pluginsConfig.pluginsDir)) {

    override fun getSystemVersion(): String = appInfo.versionBuild
    override fun createPluginFactory(): PluginFactory = LavalinkPluginFactory
    override fun createPluginLoader(): BasePluginLoader = CompoundPluginLoader()
        .add(DevelopmentPluginLoader(this), this::isDevelopment)
        .add(DefaultPluginLoader(this))
        .add(LegacyPluginLoader())

    override fun createPluginDescriptorFinder(): PluginDescriptorFinder? {
        return CompoundPluginDescriptorFinder().apply {
            add(LegacyLavalinkDescriptorFinder)
            add(LavalinkDescriptorFinder)
        }
    }

    private inner class LegacyPluginLoader : BasePluginLoader {
        override fun isApplicable(pluginPath: Path): Boolean = pluginPath.extension == "jar"
        override fun loadPlugin(pluginPath: Path, pluginDescriptor: PluginDescriptor): ClassLoader? {
            val pluginClassLoader = PluginClassLoader(
                this@PluginLoader, pluginDescriptor, javaClass.getClassLoader(),
                ClassLoadingStrategy.APD
            )
            pluginClassLoader.addFile(pluginPath.toFile())

            return pluginClassLoader
        }

    }

    private inner class LavalinkPluginClassLoader(descriptor: lavalink.server.bootstrap.PluginDescriptor) :
        PluginClassLoader(
            this@PluginLoader, descriptor, javaClass.classLoader,
            if (descriptor.manifestVersion == lavalink.server.bootstrap.PluginDescriptor.Version.V1) ClassLoadingStrategy.APD else ClassLoadingStrategy.PDA
        ) {

        init {
            definePackage(descriptor.path, Manifest(), null)
        }
    }
}
