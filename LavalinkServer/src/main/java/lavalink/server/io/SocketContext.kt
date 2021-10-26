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
import io.undertow.websockets.core.WebSocketCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.core.WebSockets
import io.undertow.websockets.jsr.UndertowSession
import lavalink.server.player.Player
import lavalink.server.config.ServerConfig
import moe.kyokobot.koe.KoeClient
import moe.kyokobot.koe.KoeEventAdapter
import moe.kyokobot.koe.VoiceConnection
import moe.kyokobot.koe.VoiceServerInfo
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class SocketContext internal constructor(
        val audioPlayerManager: AudioPlayerManager,
        val serverConfig: ServerConfig,
        private var session: WebSocketSession,
        private val socketServer: SocketServer,
        val userId: String,
        private val koe: KoeClient
) {

    companion object {
        private val log = LoggerFactory.getLogger(SocketContext::class.java)
    }

    //guildId <-> Player
    val players = ConcurrentHashMap<String, Player>()

    @Volatile
    var sessionPaused = false
    private val resumeEventQueue = ConcurrentLinkedQueue<String>()

    /** Null means disabled. See implementation notes */
    var resumeKey: String? = null
    var resumeTimeout = 60L // Seconds
    private var sessionTimeoutFuture: ScheduledFuture<Unit>? = null
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val playerUpdateService: ScheduledExecutorService

    val playingPlayers: List<Player>
        get() {
            val newList = LinkedList<Player>()
            players.values.forEach { player -> if (player.isPlaying) newList.add(player) }
            return newList
        }


    init {
        executor.scheduleAtFixedRate(StatsTask(this, socketServer), 0, 1, TimeUnit.MINUTES)

        playerUpdateService = Executors.newScheduledThreadPool(2) { r ->
            val thread = Thread(r)
            thread.name = "player-update"
            thread.isDaemon = true
            thread
        }
    }

    internal fun getPlayer(guildId: Long) = getPlayer(guildId.toString())

    internal fun getPlayer(guildId: String) = players.computeIfAbsent(guildId) {
        Player(this, guildId, audioPlayerManager, serverConfig)
    }

    internal fun getPlayers(): Map<String, Player> {
        return players
    }

    /**
     * Gets or creates a voice connection
     */
    fun getVoiceConnection(player: Player): VoiceConnection {
        val guildId = player.guildId.toLong()
        var conn = koe.getConnection(guildId)
        if (conn == null) {
            conn = koe.createConnection(guildId)
            conn.registerListener(EventHandler(player))
        }
        return conn
    }

    /**
     * Disposes of a voice connection
     */
    fun destroy(guild: Long) {
        players.remove(guild.toString())?.stop()
        koe.destroyConnection(guild)
    }

    fun pause() {
        sessionPaused = true
        sessionTimeoutFuture = executor.schedule<Unit>({
            socketServer.onSessionResumeTimeout(this)
        }, resumeTimeout, TimeUnit.SECONDS)
    }

    /**
     * Either sends the payload now or queues it up
     */
    fun send(payload: JSONObject) = send(payload.toString())

    private fun send(payload: String) {
        if (sessionPaused) {
            resumeEventQueue.add(payload)
            return
        }

        if (!session.isOpen) return

        val undertowSession = (session as StandardWebSocketSession).nativeSession as UndertowSession
        WebSockets.sendText(payload, undertowSession.webSocketChannel,
                object : WebSocketCallback<Void> {
                    override fun complete(channel: WebSocketChannel, context: Void?) {
                        log.trace("Sent {}", payload)
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
        log.info("Replaying ${resumeEventQueue.size} events")

        // Bulk actions are not guaranteed to be atomic, so we need to do this imperatively
        while (resumeEventQueue.isNotEmpty()) {
            send(resumeEventQueue.remove())
        }

        players.values.forEach { SocketServer.sendPlayerUpdate(this, it) }
    }

    internal fun shutdown() {
        log.info("Shutting down " + playingPlayers.size + " playing players.")
        executor.shutdown()
        playerUpdateService.shutdown()
        players.values.forEach(Player::stop)
        koe.close()
    }

    private inner class EventHandler(private val player: Player) : KoeEventAdapter() {
        override fun gatewayClosed(code: Int, reason: String?, byRemote: Boolean) {
            val out = JSONObject()
            out.put("op", "event")
            out.put("type", "WebSocketClosedEvent")
            out.put("guildId", player.guildId)
            out.put("reason", reason ?: "")
            out.put("code", code)
            out.put("byRemote", byRemote)

            send(out)

            SocketServer.sendPlayerUpdate(this@SocketContext, player)
        }

        override fun gatewayReady(target: InetSocketAddress?, ssrc: Int) {
            SocketServer.sendPlayerUpdate(this@SocketContext, player)
        }
    }
}
