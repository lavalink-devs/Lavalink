package dev.arbjerg.lavalink.api

import org.pf4j.PluginManager

/**
 * Interface to interact with Lavalinks plugin system.
 *
 * @property manager the [PluginManager] instance
 */
interface PluginSystem {
    val manager: PluginManager
}