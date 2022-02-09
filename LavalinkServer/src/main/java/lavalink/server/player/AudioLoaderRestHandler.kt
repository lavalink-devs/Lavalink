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
import dev.arbjerg.lavalink.api.AudioLoaderExtension
import dev.arbjerg.lavalink.api.AudioLoaderJsonModifier
import lavalink.server.config.ServerConfig
import lavalink.server.util.Util
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.concurrent.CompletionStage
import javax.servlet.http.HttpServletRequest

@RestController
class AudioLoaderRestHandler(
    private val audioPlayerManager: AudioPlayerManager,
    private val serverConfig: ServerConfig,
    private val loaderExtensions: List<AudioLoaderExtension>
) {
    companion object {
        private val log = LoggerFactory.getLogger(AudioLoaderRestHandler::class.java)

        private inline fun <reified T> Any?.intoOrNull(): T? = this as? T

        private fun log(request: HttpServletRequest) = log.info("GET ${request.servletPath}")

        private fun attemptModifier(modifier: AudioLoaderJsonModifier, json: JSONObject, block: AudioLoaderJsonModifier.(JSONObject) -> JSONObject): JSONObject? {
            return json.clone()
                .runCatching { modifier.block(this) }
                .getOrNull()
        }

        private fun AudioTrack.toJSON(): JSONObject {
            return JSONObject()
                .put("title", info.title)
                .put("author", info.author)
                .put("length", info.length)
                .put("identifier", info.identifier)
                .put("uri", info.uri)
                .put("isStream", info.isStream)
                .put("isSeekable", isSeekable)
                .put("position", position)
                .put("sourceName", sourceManager?.sourceName)
        }

        // TODO: check if this incredibly slow.
        private fun JSONObject.clone(): JSONObject = JSONObject(toMap())
    }

    private fun encodeLoadResult(result: LoadResult): JSONObject {
        val json = JSONObject()
        var playlist = JSONObject()
        val tracks = JSONArray()
        result.tracks.forEach { track ->
            var info = track.toJSON()
            if (serverConfig.allowTrackModifications) {
                (track.sourceManager as? AudioLoaderJsonModifier)?.let { source ->
                    attemptModifier(source, info) { modifyAudioTrackJson(it, track) }?.let { info = it }
                }

                for (ext in loaderExtensions) {
                    attemptModifier(ext, info) { modifyAudioTrackJson(it, track) }?.let { info = it }
                }
            }

            val trackObj = JSONObject()
            trackObj.put("info", info)

            try {
                val encoded = Util.toMessage(audioPlayerManager, track)
                trackObj.put("track", encoded)
                tracks.put(trackObj)
            } catch (e: IOException) {
                log.warn("Failed to encode a track ${track.identifier}, skipping", e)
            }
        }

        playlist.put("name", result.playlistName)
        playlist.put("selectedTrack", result.selectedTrack)

        if (serverConfig.allowTrackModifications) {
            if (result.loadResultType == ResultStatus.PLAYLIST_LOADED || result.loadResultType == ResultStatus.SEARCH_RESULT) {
                result.tracks.firstOrNull()?.sourceManager?.intoOrNull<AudioLoaderJsonModifier>()?.let { source ->
                    attemptModifier(source, playlist) { modifyAudioPlaylistJson(it, result.playlist) }?.let { playlist = it }
                }

                for (ext in loaderExtensions) {
                    attemptModifier(ext, playlist) { modifyAudioPlaylistJson(it, result.playlist) }?.let { playlist = it }
                }
            }
        }

        json.put("playlistInfo", playlist)
        json.put("loadType", result.loadResultType)
        json.put("tracks", tracks)

        if (result.loadResultType == ResultStatus.LOAD_FAILED && result.exception != null) {
            val exception = JSONObject()
            exception.put("message", result.exception.localizedMessage)
            exception.put("severity", result.exception.severity.toString())
            json.put("exception", exception)
            log.error("Track loading failed", result.exception)
        }

        return json
    }

    @GetMapping(value = ["/loadtracks"], produces = ["application/json"])
    @ResponseBody
    fun getLoadTracks(
        request: HttpServletRequest?,
        @RequestParam identifier: String?
    ): CompletionStage<ResponseEntity<String>> {
        log.info("Got request to load for identifier \"$identifier\"")
        return AudioLoader(audioPlayerManager).load(identifier)
            .thenApply(::encodeLoadResult)
            .thenApply { ResponseEntity(it.toString(), HttpStatus.OK) }
    }

    @GetMapping(value = ["/decodetrack"], produces = ["application/json"])
    @ResponseBody
    @Throws(IOException::class)
    fun getDecodeTrack(request: HttpServletRequest, @RequestParam track: String?): ResponseEntity<String> {
        log(request)

        val audioTrack = Util.toAudioTrack(audioPlayerManager, track)
        return ResponseEntity(audioTrack.toJSON().toString(), HttpStatus.OK)
    }

    @PostMapping(value = ["/decodetracks"], consumes = ["application/json"], produces = ["application/json"])
    @ResponseBody
    @Throws(IOException::class)
    fun postDecodeTracks(request: HttpServletRequest, @RequestBody body: String?): ResponseEntity<String> {
        log(request)

        val requestJSON = JSONArray(body)
        val responseJSON = JSONArray()
        for (i in 0 until requestJSON.length()) {
            val track = requestJSON.getString(i)
            val audioTrack = Util.toAudioTrack(audioPlayerManager, track)

            val trackJSON = JSONObject()
                .put("track", track)
                .put("info", audioTrack.toJSON())

            responseJSON.put(trackJSON)
        }

        return ResponseEntity(responseJSON.toString(), HttpStatus.OK)
    }
}
