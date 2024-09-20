/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
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
package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import dev.arbjerg.lavalink.protocol.v4.EncodedTracks
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.arbjerg.lavalink.protocol.v4.Tracks
import jakarta.servlet.http.HttpServletRequest
import lavalink.server.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
class AudioLoaderRestHandler(
    private val audioPlayerManager: AudioPlayerManager,
    private val pluginInfoModifiers: List<AudioPluginInfoModifier>
) {

    companion object {
        private val log = LoggerFactory.getLogger(AudioLoaderRestHandler::class.java)
    }

    @GetMapping("/v4/loadtracks")
    fun loadTracks(
        request: HttpServletRequest,
        @RequestParam identifier: String
    ): ResponseEntity<LoadResult> {
        log.info("Got request to load for identifier \"${identifier}\"")

        val item = try {
            loadAudioItem(audioPlayerManager, identifier)
        } catch (ex: FriendlyException) {
            log.error("Failed to load track", ex)
            return ResponseEntity.ok(LoadResult.loadFailed(ex))
        }

        val result = when (item) {
            null -> LoadResult.NoMatches()

            is AudioTrack -> {
                log.info("Loaded track ${item.info.title}")
                LoadResult.trackLoaded(item.toTrack(audioPlayerManager, pluginInfoModifiers))
            }

            is AudioPlaylist -> {
                log.info("Loaded playlist ${item.name}")

                val tracks = item.tracks.map { it.toTrack(audioPlayerManager, pluginInfoModifiers) }
                if (item.isSearchResult) {
                    LoadResult.searchResult(tracks)
                } else {
                    LoadResult.playlistLoaded(item.toPlaylistInfo(), item.toPluginInfo(pluginInfoModifiers), tracks)
                }
            }

            else -> {
                log.error("Unknown item type: ${item.javaClass}")
                throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Identifier returned unknown audio item type: ${item.javaClass.canonicalName}"
                )
            }
        }

        return ResponseEntity.ok(result)
    }

    @GetMapping("/v4/decodetrack")
    fun getDecodeTrack(@RequestParam encodedTrack: String?, @RequestParam track: String?): ResponseEntity<Track> {
        val trackToDecode = encodedTrack ?: track ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "No track to decode provided"
        )
        return ResponseEntity.ok(
            decodeTrack(audioPlayerManager, trackToDecode).toTrack(
                trackToDecode,
                pluginInfoModifiers
            )
        )
    }

    @PostMapping("/v4/decodetracks")
    fun decodeTracks(@RequestBody encodedTracks: EncodedTracks): ResponseEntity<Tracks> {
        if (encodedTracks.tracks.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No tracks to decode provided")
        }
        return ResponseEntity.ok(Tracks(encodedTracks.tracks.map {
            decodeTrack(audioPlayerManager, it).toTrack(it, pluginInfoModifiers)
        }))
    }
}
