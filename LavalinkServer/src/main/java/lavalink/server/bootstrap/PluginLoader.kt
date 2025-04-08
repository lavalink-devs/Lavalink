package lavalink.server.bootstrap

import lavalink.server.info.AppInfo
import org.pf4j.*
import org.pf4j.PluginDescriptor
import org.pf4j.spring.SpringExtensionFactory
import org.pf4j.spring.SpringPluginManager
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import org.pf4j.PluginLoader as BasePluginLoader

@Component
class PluginLoader(private val pluginsConfig: PluginsConfig, private val appInfo: AppInfo) :
    SpringPluginManager(Path(pluginsConfig.pluginsDir)) {

    val injector by lazy {
        LavalinkExtensionInjector(
            this,
            applicationContext.autowireCapableBeanFactory as AbstractAutowireCapableBeanFactory
        )
    }

    val extensionFinder get() = super.extensionFinder

    override fun getRuntimeMode(): RuntimeMode =
        if (pluginsConfig.developmentMode) RuntimeMode.DEVELOPMENT else RuntimeMode.DEPLOYMENT

    override fun createVersionManager(): VersionManager = FlexibleVersionManager()
    override fun getSystemVersion(): String = appInfo.versionBuild
    override fun createPluginFactory(): PluginFactory = LavalinkPluginFactory
    override fun createPluginLoader(): BasePluginLoader = CompoundPluginLoader()
        .add(DevelopmentPluginLoader(this), this::isDevelopment)
        .add(DefaultPluginLoader(this))
        .add(LegacyPluginLoader())

    override fun createExtensionFinder(): ExtensionFinder = LegacyExtensionFinder(this).apply {
        isCheckForExtensionDependencies = true
    }

    // Add auto-wiring support to extensions
    override fun createExtensionFactory(): ExtensionFactory = SpringExtensionFactory(this)

    override fun createPluginDescriptorFinder(): PluginDescriptorFinder? {
        return CompoundPluginDescriptorFinder().apply {
            add(LegacyLavalinkDescriptorFinder)
            add(LavalinkDescriptorFinder)
        }
    }

    private inner class LegacyPluginLoader : BasePluginLoader {
        override fun isApplicable(pluginPath: Path): Boolean = pluginPath.extension == "jar"
        override fun loadPlugin(pluginPath: Path, pluginDescriptor: PluginDescriptor): ClassLoader {
            val pluginClassLoader = PluginClassLoader(
                this@PluginLoader, pluginDescriptor, javaClass.getClassLoader(),
                // This is required because the old distribution format can contain classes that the server contains
                // as well, so we need the server classes to take priority
                ClassLoadingStrategy.APD
            )
            pluginClassLoader.addFile(pluginPath.toFile())

            return pluginClassLoader
        }
    }
}
