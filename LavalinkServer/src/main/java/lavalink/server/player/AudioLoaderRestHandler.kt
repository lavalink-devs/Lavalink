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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.protocol.LoadResult
import dev.arbjerg.lavalink.protocol.Track
import dev.arbjerg.lavalink.protocol.decodeTrack
import lavalink.server.util.toTrack
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletionStage
import javax.servlet.http.HttpServletRequest

@RestController
class AudioLoaderRestHandler(private val audioPlayerManager: AudioPlayerManager) {

    companion object {
        private val log = LoggerFactory.getLogger(AudioLoaderRestHandler::class.java)
    }

    private fun log(request: HttpServletRequest) {
        log.info("GET $request.servletPath")
    }

    @GetMapping(value = ["/loadtracks", "/v3/loadtracks"], produces = ["application/json"])
    @ResponseBody
    fun loadTracks(
        request: HttpServletRequest?,
        @RequestParam identifier: String?
    ): CompletionStage<ResponseEntity<LoadResult>> {
        log.info("Got request to load for identifier \"{}\"", identifier)
        return AudioLoader(audioPlayerManager).load(identifier).thenApply { ResponseEntity.ok(it) }
    }

    // we need this for backwards compatibility
    @GetMapping(value = ["/decodetrack"], produces = ["application/json"])
    @ResponseBody
    fun getDecodeTrackOld(request: HttpServletRequest, @RequestParam track: String): ResponseEntity<Track> {
        return getDecodeTrack(request, track)
    }

    @GetMapping(value = ["/v3/decodetrack"], produces = ["application/json"])
    @ResponseBody
    fun getDecodeTrack(request: HttpServletRequest, @RequestParam encodedTrack: String): ResponseEntity<Track> {
        log(request)
        val audioTrack: AudioTrack = decodeTrack(audioPlayerManager, encodedTrack)
        return ResponseEntity.ok(audioTrack.toTrack(audioPlayerManager))
    }

    @PostMapping(
        value = ["/decodetracks", "/v3/decodetracks"],
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    @ResponseBody
    fun decodeTracks(
        request: HttpServletRequest,
        @RequestBody encodedTracks: List<String>
    ): ResponseEntity<List<Track>> {
        log(request)
        return ResponseEntity.ok(encodedTracks.map {
            decodeTrack(audioPlayerManager, it).toTrack(audioPlayerManager)
        })
    }

}