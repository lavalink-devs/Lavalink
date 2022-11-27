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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.protocol.Track
import dev.arbjerg.lavalink.protocol.decodeTrack
import lavalink.server.util.toTrack
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.CompletionStage
import javax.servlet.http.HttpServletRequest

@RestController
class AudioLoaderRestHandler(
    private val audioPlayerManager: AudioPlayerManager,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private val log = LoggerFactory.getLogger(AudioLoaderRestHandler::class.java)
    }

    @GetMapping(value = ["/loadtracks", "/v3/loadtracks"])
    fun loadTracks(
        request: HttpServletRequest,
        @RequestParam identifier: String?
    ): CompletionStage<ResponseEntity<JsonNode>> {
        log.info("Got request to load for identifier \"${identifier}\"")
        return AudioLoader(audioPlayerManager).load(identifier).thenApply {
            val node: ObjectNode = objectMapper.valueToTree(it)
            if (request.servletPath.startsWith("/loadtracks") || request.servletPath.startsWith("/v3/loadtracks")) {
                if (node.get("playlistInfo").isNull) {
                    node.replace("playlistInfo", JsonNodeFactory.instance.objectNode())
                }

                if (node.get("exception").isNull) {
                    node.remove("exception")
                }
            }

            return@thenApply ResponseEntity.ok(node)
        }
    }

    @GetMapping(value = ["/decodetrack", "/v3/decodetrack"])
    fun getDecodeTrack(@RequestParam encodedTrack: String?, @RequestParam track: String?): ResponseEntity<Track> {
        val trackToDecode = encodedTrack ?: track ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "No track to decode provided"
        )
        return ResponseEntity.ok(decodeTrack(audioPlayerManager, trackToDecode).toTrack(trackToDecode))
    }

    @PostMapping(value = ["/decodetracks", "/v3/decodetracks"])
    fun decodeTracks(@RequestBody encodedTracks: List<String>): ResponseEntity<List<Track>> {
        if (encodedTracks.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No tracks to decode provided")
        }
        return ResponseEntity.ok(encodedTracks.map {
            decodeTrack(audioPlayerManager, it).toTrack(it)
        })
    }

}