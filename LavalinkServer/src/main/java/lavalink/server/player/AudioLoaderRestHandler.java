package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.server.Launcher;
import lavalink.server.util.Util;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
public class AudioLoaderRestHandler {

    private static final Logger log = LoggerFactory.getLogger(AudioLoaderRestHandler.class);

    @GetMapping("/loadtracks")
    @ResponseBody
    public String get(HttpServletRequest request, HttpServletResponse response, @RequestParam String identifier)
            throws IOException, InterruptedException {
        String path = request.getServletPath();
        log.info("GET " + path);

        response.setContentType("application/json");

        if (request.getHeader("Authorization") != null &&
                !request.getHeader("Authorization").equals(Launcher.config.getPassword())) {
            log.warn("Authorization failed");
            response.setStatus(403);
            return "";
        }

        JSONObject json = new JSONObject();
        JSONArray tracks = new JSONArray();
        List<AudioTrack> list = new AudioLoader().loadSync(identifier);

        list.forEach(track -> {
            try {
                tracks.put(Util.toMessage(track));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });

        json.put("tracks", tracks);
        return json.toString();
    }

}
