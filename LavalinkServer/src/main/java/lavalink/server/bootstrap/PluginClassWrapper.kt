package lavalink.server.bootstrap

import org.pf4j.Plugin
import org.pf4j.PluginFactory
import org.pf4j.PluginWrapper
import org.pf4j.spring.SpringPlugin
import org.pf4j.spring.SpringPluginManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PluginClassWrapper(@get:JvmName("getLavalinkContext") val wrapper: PluginWrapper) : SpringPlugin(wrapper) {
    override fun createApplicationContext(): ApplicationContext {
        val parent = (wrapper.pluginManager as SpringPluginManager).applicationContext
        return AnnotationConfigApplicationContext().apply {
            this.parent = parent
            classLoader = wrapper.pluginClassLoader
            (wrapper.descriptor as LavalinkPluginDescriptor).springConfigurationFiles.forEach {
                log.debug("Registering configuration {} from plugin {}", it, wrapper.pluginId)
                val clazz = wrapper.pluginClassLoader.loadClass(it)
                register(clazz)
            }
            refresh()
        }
    }
}

object LavalinkPluginFactory : PluginFactory {
    override fun create(pluginWrapper: PluginWrapper): Plugin = PluginClassWrapper(pluginWrapper)
}
