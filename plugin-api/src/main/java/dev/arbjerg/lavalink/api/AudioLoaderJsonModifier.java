package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;

public interface AudioLoaderJsonModifier {
    /**
     * Modifies a loaded playlist.
     *
     * @param jsonObject    JSON data that has been accumulated.
     * @param playlist      the playlist that was loaded.
     * @return the new {@link JSONObject}
     */
    JSONObject playlist(JSONObject jsonObject, AudioPlaylist playlist);

    /**
     * Modifies a loaded playlist.
     *
     * @param jsonObject    JSON data that has been accumulated.
     * @param audioTrack    the track that was loaded.
     * @return the new {@link JSONObject}
     */
    JSONObject track(JSONObject jsonObject, AudioTrack audioTrack);
}
