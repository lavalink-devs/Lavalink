/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.io

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.arbjerg.lavalink.api.ISocketServer
import dev.arbjerg.lavalink.api.PluginEventHandler
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.PlayerState
import lavalink.server.config.ServerConfig
import lavalink.server.player.LavalinkPlayer
import moe.kyokobot.koe.Koe
import moe.kyokobot.koe.KoeOptions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Service
final class SocketServer(
    private val serverConfig: ServerConfig,
    val audioPlayerManager: AudioPlayerManager,
    koeOptions: KoeOptions,
    private val eventHandlers: List<PluginEventHandler>,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>
) : TextWebSocketHandler(), ISocketServer {

    // sessionID <-> Session
    override val sessions = ConcurrentHashMap<String, SocketContext>()
    override val resumableSessions = mutableMapOf<String, SocketContext>()
    private val koe = Koe.koe(koeOptions)
    private val statsCollector = StatsCollector(this)
    private val charPool = ('a'..'z') + ('0'..'9')

    init {
        Runtime.getRuntime().addShutdownHook(ShutdownHandler(this))
    }

    companion object {
        private val log = LoggerFactory.getLogger(SocketServer::class.java)

        fun sendPlayerUpdate(socketContext: SocketContext, player: LavalinkPlayer) {
            if (socketContext.sessionPaused) return

            val connection = socketContext.getMediaConnection(player).gatewayConnection
            socketContext.sendMessage(
                    Message.Serializer,
                    Message.PlayerUpdateEvent(
                    PlayerState(
                        System.currentTimeMillis(),
                        player.audioPlayer.playingTrack?.position ?: 0,
                        connection?.isOpen == true,
                        connection?.ping ?: -1L
                    ),
                    player.guildId.toString()
                )
            )
        }
    }

    private fun generateUniqueSessionId(): String {
        var sessionId: String
        do {
            sessionId = List(16) { charPool.random() }.joinToString("")
        } while (sessions[sessionId] != null)
        return sessionId
    }

    val contexts: Collection<SocketContext>
        get() = sessions.values

    @Suppress("UastIncorrectHttpHeaderInspection")
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.handshakeHeaders.getFirst("User-Id")!!.toLong()
        var sessionId = session.handshakeHeaders.getFirst("Session-Id")
        val clientName = session.handshakeHeaders.getFirst("Client-Name")
        val userAgent = session.handshakeHeaders.getFirst("User-Agent")

        var resumable: SocketContext? = null
        if (sessionId != null) resumable = resumableSessions.remove(sessionId)

        if (resumable != null) {
            session.attributes["sessionId"] = resumable.sessionId
            sessions[resumable.sessionId] = resumable
            resumable.resume(session)
            log.info("Resumed session with id $sessionId")
            resumable.eventEmitter.onWebSocketOpen(true)
            return
        }

        sessionId = generateUniqueSessionId()
        session.attributes["sessionId"] = sessionId

        val socketContext = SocketContext(
            sessionId,
            audioPlayerManager,
            serverConfig,
            session,
            this,
            statsCollector,
            userId,
            clientName,
            koe.newClient(userId),
            eventHandlers,
            pluginInfoModifiers
        )
        sessions[sessionId] = socketContext
        socketContext.sendMessage(Message.Serializer, Message.ReadyEvent(false, sessionId))
        socketContext.eventEmitter.onWebSocketOpen(false)
        if (clientName != null) {
            log.info("Connection successfully established from $clientName")
            return
        }

        log.info("Connection successfully established")
        if (userAgent != null) {
            log.warn("Library developers: Please specify a 'Client-Name' header. User agent: $userAgent")
        } else {
            log.warn("Library developers: Please specify a 'Client-Name' header.")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val context = sessions.remove(session.attributes["sessionId"]) ?: return
        if (context.resumable) {
            resumableSessions.remove(context.sessionId)?.let { removed ->
                log.warn(
                    "Shutdown resumable session with id ${removed.sessionId} because it has the same id as a " +
                            "newly disconnected resumable session."
                )
                removed.shutdown()
            }

            resumableSessions[context.sessionId] = context
            context.pause()
            log.info(
                "Connection closed from ${session.remoteAddress} with status $status -- " +
                        "Session can be resumed within the next ${context.resumeTimeout} seconds with id ${context.sessionId}",
            )
            return
        }

        log.info("Connection closed from ${session.remoteAddress} with id ${context.sessionId} -- $status")
        context.shutdown()
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        log.warn("Lavalink v4 does not support websocket messages. Please use the REST api.")
    }

    internal fun onSessionResumeTimeout(context: SocketContext) {
        resumableSessions.remove(context.sessionId)
        context.shutdown()
    }

    internal fun canResume(id: String) = resumableSessions[id]?.stopResumeTimeout() ?: false
}
