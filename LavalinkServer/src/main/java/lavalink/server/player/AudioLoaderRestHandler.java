/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
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

package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.server.config.ServerConfig;
import lavalink.server.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AudioLoaderRestHandler {

    private static final Logger log = LoggerFactory.getLogger(AudioLoaderRestHandler.class);
    private final AudioPlayerManager audioPlayerManager;
    private final ServerConfig serverConfig;

    public AudioLoaderRestHandler(AudioPlayerManager audioPlayerManager, ServerConfig serverConfig) {
        this.audioPlayerManager = audioPlayerManager;
        this.serverConfig = serverConfig;
    }

    private void log(HttpServletRequest request) {
        String path = request.getServletPath();
        log.info("GET " + path);
    }

    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response) {
        if (request.getHeader("Authorization") == null) {
            response.setStatus(403);
            return false;
        }

        if (!request.getHeader("Authorization").equals(serverConfig.getPassword())) {
            log.warn("Authorization failed");
            response.setStatus(403);
            return false;
        }
        return true;
    }

    private JSONObject trackToJSON(AudioTrack audioTrack) {
        AudioTrackInfo trackInfo = audioTrack.getInfo();

        return new JSONObject()
                .put("title", trackInfo.title)
                .put("author", trackInfo.author)
                .put("length", trackInfo.length)
                .put("identifier", trackInfo.identifier)
                .put("uri", trackInfo.uri)
                .put("isStream", trackInfo.isStream)
                .put("isSeekable", audioTrack.isSeekable())
                .put("position", audioTrack.getPosition());
    }

    @GetMapping(value = "/loadtracks", produces = "application/json")
    @ResponseBody
    public String getLoadTracks(HttpServletRequest request, HttpServletResponse response, @RequestParam String identifier)
            throws IOException, InterruptedException {
        log(request);

        if (!isAuthorized(request, response))
            return "";

        JSONObject json = new JSONObject();
        JSONObject playlist = new JSONObject();
        JSONArray tracks = new JSONArray();
        LoadResult result = new AudioLoader(audioPlayerManager).loadSync(identifier);

        result.tracks.forEach(track -> {
            JSONObject object = new JSONObject();
            object.put("info", trackToJSON(track));

            try {
                String encoded = Util.toMessage(audioPlayerManager, track);
                object.put("track", encoded);
                tracks.put(object);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });

        playlist.put("name", result.playlistName);
        playlist.put("selectedTrack", result.selectedTrack);

        json.put("playlistInfo", playlist);
        json.put("loadType", result.loadResultType);
        json.put("tracks", tracks);

        return json.toString();
    }

    @GetMapping(value = "/decodetrack", produces = "application/json")
    @ResponseBody
    public String getDecodeTrack(HttpServletRequest request, HttpServletResponse response, @RequestParam String track) throws IOException {
        log(request);

        if (!isAuthorized(request, response))
            return "";

        AudioTrack audioTrack = Util.toAudioTrack(audioPlayerManager, track);

        return trackToJSON(audioTrack).toString();
    }

    @PostMapping(value = "/decodetracks", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String postDecodeTracks(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) throws IOException {
        log(request);

        if (!isAuthorized(request, response))
            return "";

        JSONArray requestJSON = new JSONArray(body);
        JSONArray responseJSON = new JSONArray();

        for (int i = 0; i < requestJSON.length(); i++) {
            String track = requestJSON.getString(i);
            AudioTrack audioTrack = Util.toAudioTrack(audioPlayerManager, track);

            JSONObject infoJSON = trackToJSON(audioTrack);
            JSONObject trackJSON = new JSONObject()
                    .put("track", track)
                    .put("info", infoJSON);

            responseJSON.put(trackJSON);
        }

        return responseJSON.toString();
    }
}
