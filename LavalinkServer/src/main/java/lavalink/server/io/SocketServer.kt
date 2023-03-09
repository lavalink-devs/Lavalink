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

import com.fasterxml.jackson.databind.ObjectMapper
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.api.*
import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.PlayerState
import lavalink.server.config.ServerConfig
import lavalink.server.player.LavalinkPlayer
import lavalink.server.v3.StatsCollectorV3
import moe.kyokobot.koe.Koe
import moe.kyokobot.koe.KoeOptions
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import dev.arbjerg.lavalink.protocol.v3.Message as V3Message
import dev.arbjerg.lavalink.protocol.v3.PlayerState as V3PlayerState

@Service
final class SocketServer(
    private val serverConfig: ServerConfig,
    val audioPlayerManager: AudioPlayerManager,
    koeOptions: KoeOptions,
    private val eventHandlers: List<PluginEventHandler>,
    private val webSocketExtensions: List<WebSocketExtension>,
    private val filterExtensions: List<AudioFilterExtension>,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    // sessionID <-> Session
    val contextMap = ConcurrentHashMap<String, SocketContext>()
    private val resumableSessions = mutableMapOf<String, SocketContext>()
    private val koe = Koe.koe(koeOptions)
    private val statsCollector = StatsCollector(this)
    private val statsCollectorV3 = StatsCollectorV3(this)
    private val charPool = ('a'..'z') + ('0'..'9')

    companion object {
        private val log = LoggerFactory.getLogger(SocketServer::class.java)

        fun sendPlayerUpdate(socketContext: SocketContext, player: LavalinkPlayer) {
            if (socketContext.sessionPaused) return

            val connection = socketContext.getMediaConnection(player).gatewayConnection
            if (socketContext.version == 3) {
                socketContext.sendV3Message(
                    V3Message.PlayerUpdateEvent(
                        V3PlayerState(
                            System.currentTimeMillis(),
                            player.audioPlayer.playingTrack?.position ?: 0,
                            connection?.isOpen == true,
                            connection?.ping ?: -1L
                        ),
                        player.guildId.toString(),
                    )
                )
            } else {
                socketContext.sendMessage(
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
    }

    private fun generateUniqueSessionId(): String {
        var sessionId: String
        do {
            sessionId = List(16) { charPool.random() }.joinToString("")
        } while (contextMap[sessionId] != null)
        return sessionId
    }

    val contexts: Collection<SocketContext>
        get() = contextMap.values

    @Suppress("UastIncorrectHttpHeaderInspection")
    override fun afterConnectionEstablished(session: WebSocketSession) {
        val version = if (session.uri?.path!!.startsWith("/v3")) 3 else 4
        val userId = session.handshakeHeaders.getFirst("User-Id")!!
        val resumeKey = session.handshakeHeaders.getFirst("Resume-Key")
        var sessionId = session.handshakeHeaders.getFirst("Session-Id")
        val clientName = session.handshakeHeaders.getFirst("Client-Name")
        val userAgent = session.handshakeHeaders.getFirst("User-Agent")

        session.attributes["version"] = version
        var resumable: SocketContext? = null
        if (version == 3) {
            if (resumeKey != null) resumable = resumableSessions.remove(resumeKey)
        } else {
            if (sessionId != null) resumable = resumableSessions.remove(sessionId)
        }

        if (resumable != null) {
            session.attributes["sessionId"] = resumable.sessionId
            contextMap[resumable.sessionId] = resumable
            resumable.resume(session)
            if (version == 3) {
                log.info("Resumed session with key $resumeKey")
            } else {
                log.info("Resumed session with id $sessionId")
            }
            resumable.eventEmitter.onWebSocketOpen(true)
            return
        }

        sessionId = generateUniqueSessionId()
        session.attributes["sessionId"] = sessionId

        val socketContext = SocketContext(
            sessionId,
            version,
            audioPlayerManager,
            serverConfig,
            session,
            this,
            statsCollector,
            statsCollectorV3,
            userId,
            clientName,
            koe.newClient(userId.toLong()),
            eventHandlers,
            webSocketExtensions,
            filterExtensions,
            pluginInfoModifiers,
            objectMapper
        )
        contextMap[sessionId] = socketContext
        if (version == 3) {
            socketContext.sendV3Message(V3Message.ReadyEvent(false, sessionId))
        } else {
            socketContext.sendMessage(Message.ReadyEvent(false, sessionId))
        }
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
        val context = contextMap.remove(session.attributes["sessionId"]) ?: return
        if (context.resumeKey != null) {
            resumableSessions.remove(context.resumeKey!!)?.let { removed ->
                if (context.version == 3) {
                    log.warn(
                        "Shutdown resumable session with key ${removed.resumeKey} because it has the same key as a " +
                                "newly disconnected resumable session."
                    )
                } else {
                    log.warn(
                        "Shutdown resumable session with id ${removed.sessionId} because it has the same id as a " +
                                "newly disconnected resumable session."
                    )
                }

                removed.shutdown()
            }

            resumableSessions[context.resumeKey!!] = context
            context.pause()
            if (context.version == 3) {
                log.info(
                    "Connection closed from ${session.remoteAddress} with status $status -- " +
                            "Session can be resumed within the next ${context.resumeTimeout} seconds with key ${context.resumeKey}",
                )
            } else {
                log.info(
                    "Connection closed from ${session.remoteAddress} with status $status -- " +
                            "Session can be resumed within the next ${context.resumeTimeout} seconds with id ${context.sessionId}",
                )
            }

            return
        }

        log.info("Connection closed from ${session.remoteAddress} with id ${context.sessionId} -- $status")
        context.shutdown()
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        if (session.attributes["version"] == 4) {
            log.warn("Lavalink v4 does not support websocket messages. Please use the REST api.")
            return
        }
        val json = JSONObject(message.payload)

        log.info(message.payload)

        if (!session.isOpen) {
            log.error("Ignoring closing websocket: ${session.remoteAddress!!}")
            return
        }

        val context = contextMap[session.attributes["sessionId"]]
            ?: throw IllegalStateException("No context for session ID ${session.id}. Broken websocket?")
        context.eventEmitter.onWebsocketMessageIn(message.payload)
        context.wsHandler?.handle(json)
    }

    internal fun onSessionResumeTimeout(context: SocketContext) {
        resumableSessions.remove(context.resumeKey)
        context.shutdown()
    }

    internal fun canResume(id: String) = resumableSessions[id]?.stopResumeTimeout() ?: false
}
