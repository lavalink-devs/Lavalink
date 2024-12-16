package lavalink.server.bootstrap

import org.pf4j.BasePluginLoader
import org.pf4j.DevelopmentPluginClasspath
import org.pf4j.PluginManager

private val developmentClasspath = DevelopmentPluginClasspath.GRADLE.addJarsDirectories("build/dependencies")

class DevelopmentPluginLoader(pluginManager: PluginManager) : BasePluginLoader(pluginManager, developmentClasspath)
