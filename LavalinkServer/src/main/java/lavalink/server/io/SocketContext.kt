/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
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
import lavalink.server.player.Player
import lavalink.server.util.Ws
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession
import space.npstr.magma.MagmaApi
import space.npstr.magma.MagmaMember
import space.npstr.magma.events.api.MagmaEvent
import space.npstr.magma.events.api.WebSocketClosed
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier

class SocketContext internal constructor(
        audioPlayerManagerSupplier: Supplier<AudioPlayerManager>,
        var session: WebSocketSession,
        val socketServer: SocketServer,
        val userId: String
) {

    companion object {
        private val log = LoggerFactory.getLogger(SocketContext::class.java)
    }

    val audioPlayerManager: AudioPlayerManager = audioPlayerManagerSupplier.get()
    internal val magma: MagmaApi = MagmaApi.of { socketServer.getAudioSendFactory(it) }
    //guildId <-> Player
    val players = ConcurrentHashMap<String, Player>()
    private val executor: ScheduledExecutorService
    val playerUpdateService: ScheduledExecutorService
    var sessionPaused = false

    /** Null means disabled. See implementation notes */
    var resumeKey: String? = null
    var resumeTimeout = 60L // Seconds

    val playingPlayers: List<Player>
        get() {
            val newList = LinkedList<Player>()
            players.values.forEach { player -> if (player.isPlaying) newList.add(player) }
            return newList
        }


    init {
        magma.eventStream.subscribe { this.handleMagmaEvent(it) }

        executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate(StatsTask(this, socketServer), 0, 1, TimeUnit.MINUTES)

        playerUpdateService = Executors.newScheduledThreadPool(2) { r ->
            val thread = Thread(r)
            thread.name = "player-update"
            thread.isDaemon = true
            thread
        }
    }

    internal fun getPlayer(guildId: String): Player {
        return players.computeIfAbsent(guildId
        ) { _ -> Player(this, guildId, audioPlayerManager) }
    }

    internal fun getPlayers(): Map<String, Player> {
        return players
    }

    private fun handleMagmaEvent(magmaEvent: MagmaEvent) {
        if (magmaEvent is WebSocketClosed) {
            val out = JSONObject()
            out.put("op", "event")
            out.put("type", "WebSocketClosedEvent")
            out.put("guildId", magmaEvent.member.guildId)
            out.put("reason", magmaEvent.reason)
            out.put("code", magmaEvent.closeCode)
            out.put("byRemote", magmaEvent.isByRemote)

            Ws.send(session, out)
        }
    }

    fun pauseSession() {
        sessionPaused = true
        executor.schedule({
            socketServer.onSessionResumeTimeout(this)
        }, resumeTimeout, TimeUnit.SECONDS)
    }

    fun resumeSession(session: WebSocketSession) {
        sessionPaused = false
        this.session = session
    }

    internal fun shutdown() {
        log.info("Shutting down " + playingPlayers.size + " playing players.")
        executor.shutdown()
        audioPlayerManager.shutdown()
        playerUpdateService.shutdown()
        players.keys.forEach { guildId ->
            val member = MagmaMember.builder()
                    .userId(userId)
                    .guildId(guildId)
                    .build()
            magma.removeSendHandler(member)
            magma.closeConnection(member)
        }

        players.values.forEach(Consumer<Player> { it.stop() })
        magma.shutdown()
    }
}
