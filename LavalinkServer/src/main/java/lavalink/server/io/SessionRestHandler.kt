package lavalink.server.io

import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.AudioFilterExtension
import lavalink.server.player.TrackEndMarkerHandler
import lavalink.server.player.filters.FilterChain
import lavalink.server.util.Util
import moe.kyokobot.koe.VoiceServerInfo
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
class SessionRestHandler(
    private val socketServer: SocketServer,
    private val filterExtensions: List<AudioFilterExtension>
) {

    companion object {
        private val log = LoggerFactory.getLogger(SessionRestHandler::class.java)
    }

    private fun socketContext(sessionId: String) =
        socketServer.contextMap[sessionId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found")

    @GetMapping(value = ["/v3/sessions/{sessionId}/players/{guildId}"], produces = ["application/json"])
    private fun getPlayer(
        @PathVariable sessionId: String,
        @PathVariable guildId: Long
    ): ResponseEntity<Player> {
        log.info("GET /v3/sessions/$sessionId/players/$guildId")
        val context = socketContext(sessionId)
        val player = context.getPlayer(guildId)
        val connection = context.getMediaConnection(player).gatewayConnection

        var voiceServerUpdate: VoiceServerUpdate? = null
        var discordSessionId: String? = null
        context.koe.getConnection(guildId)?.voiceServerInfo?.let {
            discordSessionId = it.sessionId
            voiceServerUpdate = VoiceServerUpdate(it.token, it.endpoint)
        }

        return ResponseEntity.ok(
            Player(
                player.guildId.toString(),
                Util.toMessage(socketServer.audioPlayerManager, player.track),
                PlayerState(
                    player.playingTrack?.position ?: 0,
                    System.currentTimeMillis(),
                    connection?.isOpen == true,
                    connection?.ping ?: -1
                ),
                player.audioPlayer.volume,
                player.isPaused,
                discordSessionId,
                voiceServerUpdate
            )
        )
    }

    @PatchMapping(
        value = ["/v3/sessions/{sessionId}/players/{guildId}"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun patchPlayer(
        @RequestBody body: String,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long
    ) {
        val json = JSONObject(body)
        log.info("PATCH /v3/sessions/$sessionId/players/$guildId: $body")
        val context = socketContext(sessionId)
        val player = context.getPlayer(guildId)

        if (json.has("sessionId") && json.has("event")) {
            log.info("Received voice server update for guild {}", guildId)
            val event = json.getJSONObject("event")

            //discord sometimes send a partial server update missing the endpoint, which can be ignored.
            if (event.has("endpoint")) {
                val voiceSessionId = json.getString("sessionId")
                val endpoint = event.getString("endpoint")
                val token = event.getString("token")
                //clear old connection
                context.koe.destroyConnection(guildId)

                val conn = context.getMediaConnection(player)
                conn.connect(VoiceServerInfo(voiceSessionId, endpoint, token)).whenComplete { _, _ ->
                    player.provideTo(conn)
                }
            }
        }

        if (json.has("pause") && !json.has("track")) { // we handle pause differently for playing new tracks
            log.info("Received pause request for guild {}", guildId)
            player.setPause(json.getBoolean("pause"))
        }

        if (json.has("volume")) {
            log.info("Received volume request for guild {}", guildId)
            player.setVolume(json.getInt("volume"))
        }

        if (json.has("position")) {
            log.info("Received seek request for guild {}", guildId)
            player.seekTo(json.getLong("position"))
            SocketServer.sendPlayerUpdate(context, player)
        }

        if (json.has("filters")) {
            log.info("Received filter request for guild {}", guildId)
            player.filters = FilterChain.parse(json, filterExtensions)
        }

        if (json.has("track")) {
            log.info("Received track request for guild {}", guildId)

            val noReplace = json.optBoolean("noReplace", false)
            if (noReplace && player.playingTrack != null) {
                log.info("Skipping play request because of noReplace")
                return
            }

            val track = Util.toAudioTrack(context.audioPlayerManager, json.getString("track"))

            player.setPause(json.optBoolean("pause", false))
            if (json.has("startTime")) {
                track.position = json.getLong("startTime")
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
            player.provideTo(context.getMediaConnection(player))
        }
    }

    @DeleteMapping("/v3/sessions/{sessionId}/players/{guildId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun deletePlayer(
        @PathVariable sessionId: String,
        @PathVariable guildId: Long
    ) {
        socketContext(sessionId).destroyPlayer(guildId)
    }

    @PatchMapping("/v3/sessions/{sessionId}", consumes = ["application/json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun patchSession(
        @RequestBody body: String,
        @PathVariable sessionId: String
    ) {
        val json = JSONObject(body)
        val context = socketContext(sessionId)
        context.resumeKey = json.optString("key", null)
        if (json.has("timeout")) context.resumeTimeout = json.getLong("timeout")
    }

    data class Player(
        val guildId: String,
        val track: String,
        val state: PlayerState,
        val volume: Int,
        val paused: Boolean,
        val lastSessionId: String?,
        val lastVoiceUpdate: VoiceServerUpdate?
    )

    data class VoiceServerUpdate(
        val Token: String,
        val Endpoint: String
    )

    data class PlayerState(
        val position: Long,
        val time: Long,
        val connected: Boolean,
        val ping: Long
    )
}
