package lavalink.server.io

import lavalink.server.util.Util
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession
import space.npstr.magma.api.MagmaMember
import space.npstr.magma.api.MagmaServerUpdate

class WebSocketHandlers(private val contextMap: Map<String, SocketContext>) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WebSocketHandlers::class.java)
    }

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
        val noReplace = json.optBoolean("noReplace", false)

        if (noReplace && player.playingTrack != null) {
            log.info("Skipping play request because of noReplace")
            return
        }

        val track = Util.toAudioTrack(ctx.audioPlayerManager, json.getString("track"))

        if (json.has("startTime")) {
            track.position = json.getLong("startTime")
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

        SocketServer.sendPlayerUpdate(ctx, player)
    }

    fun stop(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.stop()
    }

    fun pause(session: WebSocketSession, json: JSONObject) {
        val context = contextMap[session.id]!!
        val player = context.getPlayer(json.getString("guildId"))
        player.setPause(json.getBoolean("pause"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun seek(session: WebSocketSession, json: JSONObject) {
        val context = contextMap[session.id]!!
        val player = context.getPlayer(json.getString("guildId"))
        player.seekTo(json.getLong("position"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun volume(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        player.setVolume(json.getInt("volume"))
    }

    fun equalizer(session: WebSocketSession, json: JSONObject) {
        val player = contextMap[session.id]!!.getPlayer(json.getString("guildId"))
        val bands = json.getJSONArray("bands")

        for (i in 0 until bands.length()) {
            val band = bands.getJSONObject(i)
            player.setBandGain(band.getInt("band"), band.getFloat("gain"))
        }
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
        socketContext.resumeKey = json.optString("key", null)
        if (json.has("timeout")) socketContext.resumeTimeout = json.getLong("timeout")
    }
}