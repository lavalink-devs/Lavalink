package lavalink.server.io

import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.*
import lavalink.server.player.TrackEndMarkerHandler
import lavalink.server.player.filters.FilterChain
import lavalink.server.util.toPlayer
import moe.kyokobot.koe.VoiceServerInfo
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

    private fun existingPlayer(socketContext: SocketContext, guildId: Long) =
        socketContext.players[guildId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found")

    @GetMapping(value = ["/v3/sessions/{sessionId}/players/{guildId}"], produces = ["application/json"])
    private fun getPlayer(
        @PathVariable sessionId: String,
        @PathVariable guildId: Long
    ): ResponseEntity<Player> {
        log.info("GET /v3/sessions/$sessionId/players/$guildId")
        val context = socketContext(sessionId)
        val player = existingPlayer(context, guildId)

        return ResponseEntity.ok(player.toPlayer(socketServer.audioPlayerManager, context))
    }

    @PatchMapping(
        value = ["/v3/sessions/{sessionId}/players/{guildId}"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun patchPlayer(
        @RequestBody playerUpdate: PlayerUpdate,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long,
        @RequestParam noReplace: Boolean = false
    ): ResponseEntity<Player> {
        log.info("PATCH /v3/sessions/$sessionId/players/$guildId: $playerUpdate")
        val context = socketContext(sessionId)
        val player = context.getPlayer(guildId)

        playerUpdate.voice.takeIfPresent {
            log.info("Received voice server update for guild {}", guildId)
            //discord sometimes send a partial server update missing the endpoint, which can be ignored.
            if (it.Endpoint.isNotEmpty()) {
                //clear old connection
                context.koe.destroyConnection(guildId)

                val conn = context.getMediaConnection(player)
                conn.connect(VoiceServerInfo(it.sessionID, it.Token, it.Endpoint)).whenComplete { _, _ ->
                    player.provideTo(conn)
                }
            }
        }

        // we handle pause differently for playing new tracks
        playerUpdate.paused.takeIf { it.isPresent && !playerUpdate.encodedTrack.isPresent && !playerUpdate.identifier.isPresent }
            ?.let {
                log.info("Received pause request for guild {}", guildId)
                player.setPause(it.value)
            }

        playerUpdate.volume.takeIfPresent {
            log.info("Received volume request for guild {}", guildId)
            player.setVolume(it)
        }

        // we handle position differently for playing new tracks
        playerUpdate.position.takeIf { it.isPresent && !playerUpdate.encodedTrack.isPresent && !playerUpdate.identifier.isPresent }
            ?.let {
                log.info("Received seek request for guild {}", guildId)
                player.seekTo(it.value)
                SocketServer.sendPlayerUpdate(context, player)
            }

        playerUpdate.filters.takeIfPresent {
            log.info("Received filter request for guild {}", guildId)
            val filterChain = FilterChain.parse(it, filterExtensions)
            player.filters = filterChain
            SocketServer.sendPlayerUpdate(context, player)
        }

        if (playerUpdate.encodedTrack.isPresent && playerUpdate.identifier.isPresent) {
            log.info("Received encodedTrack & identifier request for guild {}", guildId)

            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both encodedTrack and identifier")
        }

        playerUpdate.encodedTrack.takeIf { it.isPresent && !playerUpdate.identifier.isPresent }?.let {
            log.info("Received encodedTrack request for guild {}", guildId)

            if (noReplace && player.playingTrack != null) {
                log.info("Skipping play request because of noReplace")
                return ResponseEntity.ok(player.toPlayer(socketServer.audioPlayerManager, context))
            }

            player.setPause(playerUpdate.paused.value)

            val track = it.value?.let { encodedTrack -> decodeTrack(context.audioPlayerManager, encodedTrack) }

            track?.let {
                playerUpdate.position.takeIfPresent { position ->
                    track.position = position
                }

                playerUpdate.endTime.takeIfPresent { endTime ->
                    if (endTime > 0) {
                        val handler = TrackEndMarkerHandler(player)
                        val marker = TrackMarker(endTime, handler)
                        track.setMarker(marker)
                    }
                }

                player.play(track)
                player.provideTo(context.getMediaConnection(player))
            } ?: player.stop()
        }

        return ResponseEntity.ok(player.toPlayer(socketServer.audioPlayerManager, context))
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
        @RequestBody sessionUpdate: SessionUpdate,
        @PathVariable sessionId: String
    ) {
        val context = socketContext(sessionId)

        sessionUpdate.resumingKey.takeIfPresent {
            context.resumeKey = it
        }

        sessionUpdate.timeout.takeIfPresent {
            context.resumeTimeout = it
        }
    }

}
