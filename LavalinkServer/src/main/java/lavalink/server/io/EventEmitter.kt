package lavalink.server.io

import dev.arbjerg.lavalink.api.IPlayer
import dev.arbjerg.lavalink.api.PluginEventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventEmitter(private val context: SocketContext, private val listeners: Collection<PluginEventHandler>) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EventEmitter::class.java)
    }

    fun onWebSocketOpen(resumed: Boolean) = iterate { it.onWebSocketOpen(context, resumed) }
    fun onSocketContextPaused() = iterate { it.onSocketContextPaused(context) }
    fun onSocketContextDestroyed() = iterate { it.onSocketContextDestroyed(context) }
    fun onWebsocketMessageIn(message: String) = iterate { it.onWebsocketMessageIn(context, message) }
    fun onWebSocketMessageOut(message: String) = iterate { it.onWebSocketMessageOut(context, message) }
    fun onNewPlayer(player: IPlayer)  = iterate { it.onNewPlayer(context, player) }
    fun onDestroyPlayer(player: IPlayer)  = iterate { it.onDestroyPlayer(context, player) }

    private fun iterate(func: (PluginEventHandler) -> Unit ) {
        listeners.forEach {
            try {
                func(it)
            } catch (e: Exception) {
                log.error("Error handling event", e)
            }
        }
    }

}