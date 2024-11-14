package lavalink.server.bootstrap

import org.pf4j.Plugin
import org.pf4j.PluginFactory
import org.pf4j.PluginWrapper
import org.pf4j.spring.SpringPlugin
import org.pf4j.spring.SpringPluginManager
import org.pf4j.util.FileUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import kotlin.io.path.useLines

class PluginClassWrapper(val context: PluginWrapper) : SpringPlugin(context) {
    override fun createApplicationContext(): ApplicationContext {
        val parent = (context.pluginManager as SpringPluginManager).applicationContext
        return AnnotationConfigApplicationContext().apply {
            this.parent = parent
            classLoader = context.pluginClassLoader
            FileUtils.getPath(context.pluginPath, "META-INF", "configurations.idx")
                .useLines {
                    it.forEach { className ->
                        log.debug("Registering configuration {} from plugin {}", className, context.pluginId)
                        val clazz = context.pluginClassLoader.loadClass(className)
                        register(clazz)
                    }
                }
            refresh()
        }
    }
}

object LavalinkPluginFactory : PluginFactory {
    override fun create(pluginWrapper: PluginWrapper): Plugin = PluginClassWrapper(pluginWrapper)
}
