package lavalink.server.io

import lavalink.server.player.FilterChain
import lavalink.server.player.VolumeConfig
import lavalink.server.util.Util
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketSession
import space.npstr.magma.MagmaMember
import space.npstr.magma.MagmaServerUpdate

class WebSocketHandlers(private val contextMap: Map<String, SocketContext>) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WebSocketHandlers::class.java)
    }

    private var loggedVolumeDeprecationWarning = false

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

        val sktContext = session.context
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
        val ctx = session.context
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
            if(!loggedVolumeDeprecationWarning) log.warn("The volume property in the play operation has been deprecated" +
                    "and will be removed in v4. Please configure a filter instead. Note that the new filter takes a " +
                    "float value with 1.0 being 100%")
            loggedVolumeDeprecationWarning = true
            val filters = player.filters ?: FilterChain()
            filters.volume = VolumeConfig(json.getFloat("volume") / 100)
            player.filters = filters
        }

        player.play(track)

        val context = session.context

        val m = MagmaMember.builder()
                .userId(context.userId)
                .guildId(json.getString("guildId"))
                .build()
        context.magma.setSendHandler(m, context.getPlayer(json.getString("guildId")))

        SocketServer.sendPlayerUpdate(ctx, player)
    }

    fun stop(session: WebSocketSession, json: JSONObject) {
        val player = session.context.getPlayer(json.getString("guildId"))
        player.stop()
    }

    fun pause(session: WebSocketSession, json: JSONObject) {
        val context = session.context
        val player = context.getPlayer(json.getString("guildId"))
        player.setPause(json.getBoolean("pause"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun seek(session: WebSocketSession, json: JSONObject) {
        val context = session.context
        val player = context.getPlayer(json.getString("guildId"))
        player.seekTo(json.getLong("position"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun volume(session: WebSocketSession, json: JSONObject) {
        val player = session.context.getPlayer(json.getString("guildId"))
        player.setVolume(json.getInt("volume"))
    }

    fun equalizer(session: WebSocketSession, json: JSONObject) {
        val player = session.context.getPlayer(json.getString("guildId"))
        val bands = json.getJSONArray("bands")

        for (i in 0 until bands.length()) {
            val band = bands.getJSONObject(i)
            player.setBandGain(band.getInt("band"), band.getFloat("gain"))
        }
    }

    fun destroy(session: WebSocketSession, json: JSONObject) {
        val socketContext = session.context
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
        val socketContext = session.context
        socketContext.resumeKey = json.optString("key", null)
        if (json.has("timeout")) socketContext.resumeTimeout = json.getLong("timeout")
    }

    fun configureFilters(session: WebSocketSession, json: JSONObject) {
        val player = session.context.getPlayer(json.getString("guildId"))
        val filters = player.filters ?: FilterChain()
        filters.parse(json)
        player.filters = filters
    }

    private val WebSocketSession.context get() = contextMap[this.id] ?: error("Unknown context for WS session")
}