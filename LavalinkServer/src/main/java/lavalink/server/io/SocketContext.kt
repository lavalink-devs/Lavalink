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
import dev.arbjerg.lavalink.protocol.v3.Message
import dev.arbjerg.lavalink.protocol.v4.json
import io.undertow.websockets.core.WebSocketCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import io.undertow.websockets.jsr.UndertowSession
import kotlinx.serialization.SerializationStrategy
import lavalink.server.config.ServerConfig
import lavalink.server.player.LavalinkPlayer
import lavalink.server.v3.StatsCollectorV3
import lavalink.server.v3.WebSocketHandlerV3
import moe.kyokobot.koe.KoeClient
import moe.kyokobot.koe.KoeEventAdapter
import moe.kyokobot.koe.MediaConnection
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.*

class SocketContext(
    private val sessionId: String,
    val version: Int,
    val audioPlayerManager: AudioPlayerManager,
    private val serverConfig: ServerConfig,
    private var session: WebSocketSession,
    private val socketServer: SocketServer,
    statsCollector: StatsCollector,
    statsCollectorV3: StatsCollectorV3,
    private val userId: String,
    private val clientName: String?,
    val koe: KoeClient,
    eventHandlers: Collection<PluginEventHandler>,
    webSocketExtensions: List<WebSocketExtension>,
    filterExtensions: List<AudioFilterExtension>,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>,
    private val objectMapper: ObjectMapper
) : ISocketContext {

    companion object {
        private val log = LoggerFactory.getLogger(SocketContext::class.java)
    }

    //guildId <-> LavalinkPlayer
    private val players = ConcurrentHashMap<Long, LavalinkPlayer>()

    val eventEmitter = EventEmitter(this, eventHandlers)
    val wsHandler = if (version == 3) WebSocketHandlerV3(this, webSocketExtensions, filterExtensions, serverConfig, objectMapper) else null

    @Volatile
    var sessionPaused = false
    private val resumeEventQueue = ConcurrentLinkedQueue<String>()

    /** Null means disabled. See implementation notes */
    var resumeKey: String? = null
    var resumeTimeout = 60L // Seconds
    private var sessionTimeoutFuture: ScheduledFuture<Unit>? = null
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val playerUpdateService: ScheduledExecutorService

    val playingPlayers: List<LavalinkPlayer>
        get() {
            val newList = LinkedList<LavalinkPlayer>()
            players.values.forEach { player -> if (player.isPlaying) newList.add(player) }
            return newList
        }


    init {
        val task = if (version == 3) {
            statsCollectorV3.createTask(this)
        } else {
            statsCollector.createTask(this)
        }
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES)
        playerUpdateService = Executors.newScheduledThreadPool(2) { r ->
            val thread = Thread(r)
            thread.name = "player-update"
            thread.isDaemon = true
            thread
        }
    }

    override fun getSessionId(): String {
        return sessionId
    }

    override fun getUserId(): Long {
        return userId.toLong()
    }

    override fun getClientName(): String? {
        return clientName
    }

    override fun getPlayer(guildId: Long) = players.computeIfAbsent(guildId) {
        val player = LavalinkPlayer(this, guildId, serverConfig, audioPlayerManager, pluginInfoModifiers)
        eventEmitter.onNewPlayer(player)
        player
    }

    override fun getPlayers(): Map<Long, LavalinkPlayer> {
        return players.toMap()
    }

    /**
     * Gets or creates a media connection
     */
    fun getMediaConnection(player: LavalinkPlayer): MediaConnection {
        val guildId = player.guildId
        var conn = koe.getConnection(guildId)
        if (conn == null) {
            conn = koe.createConnection(guildId)
            conn.registerListener(WsEventHandler(player))
        }
        return conn
    }

    /**
     * Disposes of a voice connection
     */
    override fun destroyPlayer(guild: Long) {
        val player = players.remove(guild)
        if (player != null) {
            eventEmitter.onDestroyPlayer(player)
            player.destroy()
        }
        koe.destroyConnection(guild)
    }

    fun pause() {
        sessionPaused = true
        sessionTimeoutFuture = executor.schedule<Unit>({
            socketServer.onSessionResumeTimeout(this)
        }, resumeTimeout, TimeUnit.SECONDS)
        eventEmitter.onSocketContextPaused()
    }

    override fun <T : Any?> sendMessage(serializer: SerializationStrategy<T>, message: T) {
        send(json.encodeToString(serializer, message))
    }

    override fun getState(): ISocketContext.State = when {
        session.isOpen -> ISocketContext.State.OPEN
        sessionPaused -> ISocketContext.State.RESUMABLE
        else -> ISocketContext.State.DESTROYED
    }

    /**
     * Either sends the payload now or queues it up
     */
    private fun send(payload: String) {
        eventEmitter.onWebSocketMessageOut(payload)

        if (sessionPaused) {
            resumeEventQueue.add(payload)
            return
        }

        if (!session.isOpen) return

        val undertowSession = (session as StandardWebSocketSession).nativeSession as UndertowSession
        WebSockets.sendText(payload, undertowSession.webSocketChannel,
            object : WebSocketCallback<Void> {
                override fun complete(channel: WebSocketChannel, context: Void?) {
                    log.trace("Sent $payload")
                }

                override fun onError(channel: WebSocketChannel, context: Void?, throwable: Throwable) {
                    log.error("Error", throwable)
                }
            })
    }

    /**
     * @return true if we can resume, false otherwise
     */
    fun stopResumeTimeout() = sessionTimeoutFuture?.cancel(false) ?: false

    fun resume(session: WebSocketSession) {
        sessionPaused = false
        this.session = session
        sendMessage(Message.ReadyEvent(true, sessionId))
        log.info("Replaying ${resumeEventQueue.size} events")

        // Bulk actions are not guaranteed to be atomic, so we need to do this imperatively
        while (resumeEventQueue.isNotEmpty()) {
            send(resumeEventQueue.remove())
        }

        players.values.forEach { SocketServer.sendPlayerUpdate(this, it) }
    }

    internal fun shutdown() {
        log.info("Shutting down ${playingPlayers.size} playing players.")
        executor.shutdown()
        playerUpdateService.shutdown()
        players.values.forEach {
            this.destroyPlayer(it.guildId)
        }
        koe.close()
        eventEmitter.onSocketContextDestroyed()
    }

    override fun closeWebSocket(closeCode: Int, reason: String?) {
        session.close(CloseStatus(closeCode, reason))
    }

    override fun closeWebSocket(closeCode: Int) {
        closeWebSocket(closeCode, null)
    }

    override fun closeWebSocket() {
        session.close()
    }

    private inner class WsEventHandler(private val player: LavalinkPlayer) : KoeEventAdapter() {
        override fun gatewayClosed(code: Int, reason: String?, byRemote: Boolean) {
            val event = Message.WebSocketClosedEvent(code, reason ?: "", byRemote, player.guildId.toString())
            sendMessage(event)
            SocketServer.sendPlayerUpdate(this@SocketContext, player)
        }

        override fun gatewayReady(target: InetSocketAddress?, ssrc: Int) {
            SocketServer.sendPlayerUpdate(this@SocketContext, player)
        }
    }
}
