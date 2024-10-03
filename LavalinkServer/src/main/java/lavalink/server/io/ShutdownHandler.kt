package lavalink.server.io

import org.springframework.web.socket.CloseStatus

class ShutdownHandler(private val socketServer: SocketServer) : Thread("lavalink-shutdown-handler") {
    init {
        isDaemon = false // we want this thread to block shutdown until it has finished running
    }

    override fun run() {
        socketServer.contexts.forEach {
            // don't care about exceptions here, the JVM's shutting down anyway.
            it.runCatching { closeWebSocket(CloseStatus.GOING_AWAY.code) }
        }
    }
}
