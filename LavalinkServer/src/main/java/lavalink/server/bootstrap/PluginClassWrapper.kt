package lavalink.server.bootstrap

import org.pf4j.Plugin
import org.pf4j.PluginFactory
import org.pf4j.PluginWrapper

class PluginClassWrapper(@get:JvmName("getLavalinkContext") val wrapper: PluginWrapper) : Plugin()

object LavalinkPluginFactory : PluginFactory {
    override fun create(pluginWrapper: PluginWrapper): Plugin = PluginClassWrapper(pluginWrapper)
}
