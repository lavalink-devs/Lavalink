package lavalink.server.io

import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import lavalink.server.player.TrackEndMarkerHandler
import lavalink.server.util.Util
import moe.kyokobot.koe.VoiceServerInfo
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WebSocketHandlers(private val contextMap: Map<String, SocketContext>) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(WebSocketHandlers::class.java)
    }

    fun voiceUpdate(context: SocketContext, json: JSONObject) {
        val sessionId = json.getString("sessionId")
        val guildId = json.getLong("guildId")

        val event = json.getJSONObject("event")
        val endpoint: String? = event.optString("endpoint")
        val token: String = event.getString("token")

        //discord sometimes send a partial server update missing the endpoint, which can be ignored.
        endpoint ?: return

        context.getVoiceConnection(guildId).connect(VoiceServerInfo(sessionId, endpoint, token))
    }

    fun play(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        val noReplace = json.optBoolean("noReplace", false)

        if (noReplace && player.playingTrack != null) {
            log.info("Skipping play request because of noReplace")
            return
        }

        val track = Util.toAudioTrack(context.audioPlayerManager, json.getString("track"))

        if (json.has("startTime")) {
            track.position = json.getLong("startTime")
        }

        player.setPause(json.optBoolean("pause", false))
        if (json.has("volume")) {
            player.setVolume(json.getInt("volume"))
        }

        if (json.has("endTime")) {
            val stopTime = json.getLong("endTime")
            if (stopTime > 0) {
                val handler = TrackEndMarkerHandler(player)
                val marker = TrackMarker(stopTime, handler)
                track.setMarker(marker)
            }
        }

        player.play(track)

        val conn = context.getVoiceConnection(player.guildId.toLong())
        context.getPlayer(json.getString("guildId")).provideTo(conn)
    }

    fun stop(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        player.stop()
    }

    fun pause(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        player.setPause(json.getBoolean("pause"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun seek(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        player.seekTo(json.getLong("position"))
        SocketServer.sendPlayerUpdate(context, player)
    }

    fun volume(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        player.setVolume(json.getInt("volume"))
    }

    fun equalizer(context: SocketContext, json: JSONObject) {
        val player = context.getPlayer(json.getString("guildId"))
        val bands = json.getJSONArray("bands")

        for (i in 0 until bands.length()) {
            val band = bands.getJSONObject(i)
            player.setBandGain(band.getInt("band"), band.getFloat("gain"))
        }
    }

    fun destroy(context: SocketContext, json: JSONObject) {
        context.destroy(json.getLong("guildId"))
    }

    fun configureResuming(context: SocketContext, json: JSONObject) {
        context.resumeKey = json.optString("key", null)
        if (json.has("timeout")) context.resumeTimeout = json.getLong("timeout")
    }
}
