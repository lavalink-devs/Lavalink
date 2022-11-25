package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.*
import lavalink.server.config.ServerConfig
import lavalink.server.io.SocketServer
import lavalink.server.io.WebSocketHandler
import lavalink.server.player.filters.FilterChain
import lavalink.server.util.*
import moe.kyokobot.koe.VoiceServerInfo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletableFuture

@RestController
class PlayerRestHandler(
    private val socketServer: SocketServer,
    private val filterExtensions: List<AudioFilterExtension>,
    private val serverConfig: ServerConfig,
) {

    companion object {
        private val log = LoggerFactory.getLogger(PlayerRestHandler::class.java)
    }

    @GetMapping(value = ["/v3/sessions/{sessionId}/players"], produces = ["application/json"])
    private fun getPlayers(@PathVariable sessionId: String): ResponseEntity<Players> {
        val context = socketContext(socketServer, sessionId)

        return ResponseEntity.ok(Players(context.players.values.map { it.toPlayer(context) }))
    }

    @GetMapping(value = ["/v3/sessions/{sessionId}/players/{guildId}"], produces = ["application/json"])
    private fun getPlayer(@PathVariable sessionId: String, @PathVariable guildId: Long): ResponseEntity<Player> {
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
        @RequestBody playerUpdate: PlayerUpdate,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long,
        @RequestParam noReplace: Boolean = false
    ): ResponseEntity<Player> {
        if (playerUpdate.encodedTrack.isPresent && playerUpdate.identifier.isPresent) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both encodedTrack and identifier")
        }

        val context = socketContext(socketServer, sessionId)
        val player = context.getPlayer(guildId)

        playerUpdate.voice.takeIfPresent {
            //discord sometimes send a partial server update missing the endpoint, which can be ignored.
            if (it.endpoint.isEmpty() || it.token.isEmpty() || it.sessionId.isEmpty()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Partial voice state update")
            }
            //clear old connection
            context.koe.destroyConnection(guildId)

            val conn = context.getMediaConnection(player)
            conn.connect(VoiceServerInfo(it.sessionId, it.endpoint, it.token)).exceptionally {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to voice server")
            }.toCompletableFuture().join()
            player.provideTo(conn)
        }

        // we handle pause differently for playing new tracks
        playerUpdate.paused.takeIf { it.isPresent && !playerUpdate.encodedTrack.isPresent && !playerUpdate.identifier.isPresent }
            ?.let {
                player.setPause(it.value)
            }

        playerUpdate.volume.takeIfPresent {
            player.setVolume(it)
        }

        // we handle position differently for playing new tracks
        playerUpdate.position.takeIf { it.isPresent && !playerUpdate.encodedTrack.isPresent && !playerUpdate.identifier.isPresent }
            ?.let {
                player.seekTo(it.value)
                SocketServer.sendPlayerUpdate(context, player)
            }

        playerUpdate.filters.takeIfPresent {
            player.filters = FilterChain.parse(it, filterExtensions, serverConfig.filters)
            SocketServer.sendPlayerUpdate(context, player)
        }

        if (playerUpdate.encodedTrack.isPresent || playerUpdate.identifier.isPresent) {

            if (noReplace && player.track != null) {
                log.info("Skipping play request because of noReplace")
                return ResponseEntity.ok(player.toPlayer(context))
            }
            player.setPause(if (playerUpdate.paused.isPresent) playerUpdate.paused.value else false)

            val track: AudioTrack? = if (playerUpdate.encodedTrack.isPresent) {
                playerUpdate.encodedTrack.value?.let { encodedTrack ->
                    decodeTrack(context.audioPlayerManager, encodedTrack)
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

                    override fun loadFailed(exception: FriendlyException) {
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
    private fun deletePlayer(@PathVariable sessionId: String, @PathVariable guildId: Long) {
        socketContext(socketServer, sessionId).destroyPlayer(guildId)
    }
}


