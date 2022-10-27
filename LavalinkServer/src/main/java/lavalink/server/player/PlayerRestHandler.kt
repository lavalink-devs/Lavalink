package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.*
import lavalink.server.io.SocketServer
import lavalink.server.player.filters.FilterChain
import lavalink.server.util.*
import moe.kyokobot.koe.VoiceServerInfo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest

@RestController
class PlayerRestHandler(
    private val socketServer: SocketServer, private val filterExtensions: List<AudioFilterExtension>
) {

    companion object {
        private val log = LoggerFactory.getLogger(PlayerRestHandler::class.java)
    }

    @GetMapping(value = ["/v3/sessions/{sessionId}/players"], produces = ["application/json"])
    private fun getPlayers(
        request: HttpServletRequest,
        @PathVariable sessionId: String,
    ): ResponseEntity<Players> {
        logRequest(log, request)
        val context = socketContext(socketServer, sessionId)

        return ResponseEntity.ok(Players(context.players.values.map { it.toPlayer(context) }))
    }

    @GetMapping(value = ["/v3/sessions/{sessionId}/players/{guildId}"], produces = ["application/json"])
    private fun getPlayer(
        request: HttpServletRequest,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long
    ): ResponseEntity<Player> {
        logRequest(log, request)
        val context = socketContext(socketServer, sessionId)
        val player = existingPlayer(context, guildId)

        return ResponseEntity.ok(player.toPlayer(context))
    }

    @PatchMapping(
        value = ["/v3/sessions/{sessionId}/players/{guildId}"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun patchPlayer(
        request: HttpServletRequest,
        @RequestBody playerUpdate: PlayerUpdate,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long,
        @RequestParam noReplace: Boolean = false
    ): ResponseEntity<Player> {
        logRequest(log, request)

        if (playerUpdate.encodedTrack.isPresent && playerUpdate.identifier.isPresent) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both encodedTrack and identifier")
        }

        val context = socketContext(socketServer, sessionId)
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

        if (playerUpdate.encodedTrack.isPresent || playerUpdate.identifier.isPresent) {
            log.info("Received encodedTrack or identifier request for guild {}", guildId)

            if (noReplace && player.playingTrack != null) {
                log.info("Skipping play request because of noReplace")
                return ResponseEntity.ok(player.toPlayer(context))
            }
            player.setPause(playerUpdate.paused.value)

            val track: AudioTrack? = if (playerUpdate.encodedTrack.isPresent) {
                playerUpdate.encodedTrack.value?.let { encodedTrack ->
                    decodeTrack(
                        context.audioPlayerManager, encodedTrack
                    )
                }
            } else {
                val trackFuture = CompletableFuture<AudioTrack>()
                context.audioPlayerManager.loadItem(playerUpdate.identifier.value, object : AudioLoadResultHandler {
                    override fun trackLoaded(track: AudioTrack) {
                        trackFuture.complete(track)
                    }

                    override fun playlistLoaded(playlist: AudioPlaylist?) {
                        trackFuture.completeExceptionally(IllegalArgumentException("Cannot play a playlist"))
                    }

                    override fun noMatches() {
                        trackFuture.completeExceptionally(IllegalArgumentException("No track found for identifier ${playerUpdate.identifier.value}"))
                    }

                    override fun loadFailed(exception: FriendlyException?) {
                        trackFuture.completeExceptionally(exception)
                    }
                })

                trackFuture.exceptionally {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, it.message, getRootCause(it))
                }.join()
            }

            track?.let {
                playerUpdate.position.takeIfPresent { position ->
                    track.position = position
                }

                playerUpdate.endTime.takeIfPresent { endTime ->
                    if (endTime > 0) {
                        track.setMarker(TrackMarker(endTime, TrackEndMarkerHandler(player)))
                    }
                }

                player.play(track)
                player.provideTo(context.getMediaConnection(player))
            } ?: player.stop()
        }

        return ResponseEntity.ok(player.toPlayer(context))
    }

    @DeleteMapping("/v3/sessions/{sessionId}/players/{guildId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun deletePlayer(
        request: HttpServletRequest,
        @PathVariable sessionId: String, @PathVariable guildId: Long
    ) {
        logRequest(log, request)
        socketContext(socketServer, sessionId).destroyPlayer(guildId)
    }
}


