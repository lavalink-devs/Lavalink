package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.arbjerg.lavalink.protocol.v4.*
import lavalink.server.config.ServerConfig
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

@RestController
class PlayerRestHandler(
    private val socketServer: SocketServer,
    private val filterExtensions: List<AudioFilterExtension>,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>,
    serverConfig: ServerConfig,
) {

    companion object {
        private val log = LoggerFactory.getLogger(PlayerRestHandler::class.java)
    }

    val disabledFilters = serverConfig.filters.entries.filter { !it.value }.map { it.key }

    @GetMapping("/v4/sessions/{sessionId}/players")
    private fun getPlayers(@PathVariable sessionId: String): ResponseEntity<Players> {
        val context = socketContext(socketServer, sessionId)

        return ResponseEntity.ok(Players(context.players.values.map { it.toPlayer(context, pluginInfoModifiers) }))
    }

    @GetMapping("/v4/sessions/{sessionId}/players/{guildId}")
    private fun getPlayer(@PathVariable sessionId: String, @PathVariable guildId: Long): ResponseEntity<Player> {
        val context = socketContext(socketServer, sessionId)
        val player = existingPlayer(context, guildId)

        return ResponseEntity.ok(player.toPlayer(context, pluginInfoModifiers))
    }

    @PatchMapping("/v4/sessions/{sessionId}/players/{guildId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun patchPlayer(
        @RequestBody playerUpdate: PlayerUpdate,
        @PathVariable sessionId: String,
        @PathVariable guildId: Long,
        @RequestParam noReplace: Boolean = false
    ): ResponseEntity<Player> {
        val context = socketContext(socketServer, sessionId)

        if (playerUpdate.track.isPresent() && (playerUpdate.encodedTrack is Omissible.Present || playerUpdate.identifier is Omissible.Present)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both track and encodedTrack/identifier")
        }

        val track = if (playerUpdate.track.isPresent()) {
            playerUpdate.track
        } else {
            if (playerUpdate.encodedTrack is Omissible.Present || playerUpdate.identifier is Omissible.Present) {
                PlayerUpdateTrack(
                    playerUpdate.encodedTrack,
                    playerUpdate.identifier
                ).toOmissible()
            } else {
                Omissible.Omitted()
            }
        }

        val encodedTrack = track.ifPresent { it.encoded } ?: Omissible.Omitted()
        val identifier = track.ifPresent { it.identifier } ?: Omissible.Omitted()
        val userData = track.ifPresent { it.userData } ?: Omissible.Omitted()

        if (encodedTrack is Omissible.Present && identifier is Omissible.Present) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot specify both encodedTrack and identifier")
        }

        playerUpdate.filters.ifPresent { filters ->
            val invalidFilters = filters.validate(disabledFilters)

            if (invalidFilters.isNotEmpty()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Following filters are disabled in the config: ${invalidFilters.joinToString()}"
                )
            }
        }

        playerUpdate.voice.ifPresent {
            // Discord sometimes sends a partial voice server update missing the endpoint, which can be ignored.
            if (it.endpoint.isEmpty() || it.token.isEmpty() || it.sessionId.isEmpty()) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Partial Lavalink voice state: $it")
            }
        }

        playerUpdate.endTime.ifPresent {
            if (it != null && it <= 0) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be greater than 0")
            }
        }

        val player = context.getPlayer(guildId)

        playerUpdate.voice.ifPresent {
            val oldConn = context.koe.getConnection(guildId)
            if (oldConn == null ||
                oldConn.gatewayConnection?.isOpen == false ||
                oldConn.voiceServerInfo == null ||
                oldConn.voiceServerInfo?.endpoint != it.endpoint ||
                oldConn.voiceServerInfo?.token != it.token ||
                oldConn.voiceServerInfo?.sessionId != it.sessionId
            ) {
                //clear old connection
                context.koe.destroyConnection(guildId)

                val conn = context.getMediaConnection(player)
                conn.connect(VoiceServerInfo(it.sessionId, it.endpoint, it.token)).exceptionally {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect to voice server")
                }.toCompletableFuture().join()
                player.provideTo(conn)
            }
        }

        // we handle pause differently for playing new tracks
        val paused = playerUpdate.paused
        paused.takeIfPresent { encodedTrack is Omissible.Omitted && identifier is Omissible.Omitted }
            ?.let {
                player.setPause(it)
            }

        // we handle userData differently for playing new tracks
        userData.takeIfPresent { encodedTrack is Omissible.Omitted && identifier is Omissible.Omitted }
            ?.let {
                player.track?.userData = it
            }

        playerUpdate.volume.ifPresent {
            player.setVolume(it)
        }

        // we handle position differently for playing new tracks
        playerUpdate.position.takeIfPresent { encodedTrack is Omissible.Omitted && identifier is Omissible.Omitted }
            ?.let {
                if (player.track != null) {
                    player.seekTo(it)
                    SocketServer.sendPlayerUpdate(context, player)
                }
            }

        playerUpdate.endTime.takeIfPresent { encodedTrack is Omissible.Omitted && identifier is Omissible.Omitted }
            ?.let { endTime ->
                val marker = TrackMarker(endTime, TrackEndMarkerHandler(player))
                player.track?.setMarker(marker)
            }

        playerUpdate.filters.ifPresent {
            player.filters = FilterChain.parse(it, filterExtensions)
            SocketServer.sendPlayerUpdate(context, player)
        }

        if (encodedTrack is Omissible.Present || identifier is Omissible.Present) {

            if (noReplace && player.track != null) {
                log.info("Skipping play request because of noReplace")
                return ResponseEntity.ok(player.toPlayer(context, pluginInfoModifiers))
            }
            player.setPause(if (paused is Omissible.Present) paused.value else false)

            val newTrack: AudioTrack? = if (encodedTrack is Omissible.Present) {
                encodedTrack.value?.let {
                    decodeTrack(context.audioPlayerManager, it)
                }
            } else {
                val trackFuture = CompletableFuture<AudioTrack>()
                context.audioPlayerManager.loadItemSync(
                    (identifier as Omissible.Present).value,
                    object : AudioLoadResultHandler {
                        override fun trackLoaded(track: AudioTrack) {
                            trackFuture.complete(track)
                        }

                        override fun playlistLoaded(playlist: AudioPlaylist) {
                            trackFuture.completeExceptionally(
                                ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Cannot play a playlist or search result"
                                )
                            )
                        }

                        override fun noMatches() {
                            trackFuture.completeExceptionally(
                                ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "No matches found for identifier"
                                )
                            )
                        }

                        override fun loadFailed(exception: FriendlyException) {
                            trackFuture.completeExceptionally(
                                ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    exception.message,
                                    getRootCause(exception)
                                )
                            )
                        }
                    })

                trackFuture.join()
            }

            newTrack?.let {
                playerUpdate.position.ifPresent { position ->
                    newTrack.position = position
                }

                userData.ifPresent { userData ->
                    newTrack.userData = userData
                }

                playerUpdate.endTime.ifPresent { endTime ->
                    if (endTime != null) {
                        newTrack.setMarker(TrackMarker(endTime, TrackEndMarkerHandler(player)))
                    }
                }

                player.play(newTrack)
                player.provideTo(context.getMediaConnection(player))
            } ?: player.stop()
        }

        return ResponseEntity.ok(player.toPlayer(context, pluginInfoModifiers))
    }

    @DeleteMapping("/v4/sessions/{sessionId}/players/{guildId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    private fun deletePlayer(@PathVariable sessionId: String, @PathVariable guildId: Long) {
        socketContext(socketServer, sessionId).destroyPlayer(guildId)
    }
}


