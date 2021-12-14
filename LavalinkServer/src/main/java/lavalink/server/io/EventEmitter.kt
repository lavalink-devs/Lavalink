package lavalink.server.io

import dev.arbjerg.lavalink.api.IPlayer
import dev.arbjerg.lavalink.api.PluginEventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventEmitter(private val context: SocketContext, private val listeners: Collection<PluginEventHandler>) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EventEmitter::class.java)
    }

    fun onWebSocketOpen() = listeners.iterate { it.onWebSocketOpen(context) }

    fun onWebSocketClose() = listeners.iterate { it.onWebSocketClose(context) }

    fun onWebsocketMessageIn(message: String) = listeners.iterate { it.onWebsocketMessageIn(context, message) }

    fun onWebSocketMessageOut(message: String) = listeners.iterate { it.onWebSocketMessageOut(context, message) }

    fun onNewPlayer(player: IPlayer)  = listeners.iterate { it.onNewPlayer(context, player) }

    private fun <V> Collection<V>.iterate(func: (V) -> Unit ) {
        forEach {
            try {
                func(it)
            } catch (e: Exception) {
                log.error("Error handling event", e)
            }
        }
    }

}