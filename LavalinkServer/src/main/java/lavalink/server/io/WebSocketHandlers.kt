package lavalink.server.io

import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import lavalink.server.player.TrackEndMarkerHandler
import lavalink.server.util.Util
import lavalink.server.util.Ws
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.web.socket.WebSocketSession
import space.npstr.magma.MagmaMember
import space.npstr.magma.MagmaServerUpdate
import java.util.HashMap

class WebSocketHandlers(socketServer: SocketServer) {

    private val contextMap = socketServer.contextMap

    fun voiceUpdate(session: WebSocketSession, json: JSONObject) {
        val sessionId = json.getString("sessionId")
        val guildId = json.getString("guildId")

        val event = json.getJSONObject("event")
        val endpoint = event.optString("endpoint")
        val token = event.getString("token")

        //discord sometimes send a partial server update missing the endpoint, which can be ignored.
        if (endpoint == null || endpoint.isEmpty()) {
            return
        }

        val sktContext = contextMap[session.id]!!
        val member = MagmaMember.builder()
                .userId(sktContext.userId)
                .guildId(guildId)
                .build()
        val serverUpdate = MagmaServerUpdate.builder()
                .sessionId(sessionId)
                .endpoint(endpoint)
                .token(token)
                .build()
        sktContext.magma.provideVoiceServerUpdate(member, serverUpdate)
    }

    fun play(session: WebSocketSession, json: JSONObject) {
        val ctx = contextMap[session.id]!!
        val player = ctx.getPlayer(json.getString("guildId"))
        val track = Util.toAudioTrack(ctx.audioPlayerManager, json.getString("track"))
        if (json.has("startTime")) {
            track.position = json.getLong("startTime")
        }
        if (json.has("endTime")) {
            track.setMarker(TrackMarker(json.getLong("endTime"), TrackEndMarkerHandler(player)))
        }

        player.setPause(json.optBoolean("pause", false))
        if (json.has("volume")) {
            player.setVolume(json.getInt("volume"))
        }

        player.play(track)

        val context = contextMap[session.id]!!

        val m = MagmaMember.builder()
                .userId(context.userId)
                .guildId(json.getString("guildId"))
                .build()
        context.magma.setSendHandler(m, context.getPlayer(json.getString("guildId")))

        SocketServer.sendPlayerUpdate(session, player)
    }

    fun stop(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.stop()
    }

    fun pause(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.setPause(json.getBoolean("pause"))
        SocketServer.sendPlayerUpdate(session, player)
    }

    fun seek(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.seekTo(json.getLong("position"))
        SocketServer.sendPlayerUpdate(session, player)
    }

    fun volume(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.setVolume(json.getInt("volume"))
    }

    fun destroy(session: WebSocketSession, json: JSONObject) {
        val socketContext = contextMap[session.id]!!
        val player = socketContext.players.remove(json.getString("guildId"))
        player?.stop()
        val mem = MagmaMember.builder()
                .userId(socketContext.userId)
                .guildId(json.getString("guildId"))
                .build()
        socketContext.magma.removeSendHandler(mem)
        socketContext.magma.closeConnection(mem)
    }

    fun configureResuming(session: WebSocketSession, json: JSONObject) {
        val socketContext = contextMap[session.id]!!
        socketContext.resumeKey = json.optString("key")
        if (json.has("timeout")) socketContext.resumeTimeout = json.getLong("timeout")
    }

    fun reqState(session: WebSocketSession, json: JSONObject) {
        val context = contextMap[session.id]!!
        val guildIds: List<String> = if (json.optBoolean("getAll")) {
            context.players.keys().toList()
        } else {
            listOf(json.getString("guildId")!!)
        }

        val out = JSONObject().apply {
            put("op", "resState")
            put("time", System.currentTimeMillis())
        }

        val array = JSONArray()

        guildIds.forEach {
            val player = context.players[it]
            val playerJson = if (player != null) {
                JSONObject().apply {
                    val track = player.playingTrack
                    if (track != null) {
                        put("track", Util.toMessage(context.audioPlayerManager, track))
                        put("position", track.position)
                    } else {
                        put("track", JSONObject.NULL)
                        put("position", JSONObject.NULL)
                    }
                    put("paused", player.isPaused)
                }
            } else null

            val guildJson = JSONObject().apply {
                put("guildId", it)
                put("player", playerJson ?: JSONObject.NULL)
            }
            array.put(guildJson)
        }
        out.put("guilds", array)
        Ws.send(session, out)
    }

}