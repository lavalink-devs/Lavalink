package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;

public interface AudioLoaderJsonModifier {
    /**
     * Modifies an {@link AudioPlaylist}'s JSON.
     *
     * @param jsonObject JSON data that has been accumulated.
     * @param playlist   the playlist that was loaded.
     * @return the new {@link JSONObject}
     */
    JSONObject modifyAudioPlaylistJson(JSONObject jsonObject, AudioPlaylist playlist);

    /**
     * Modifies an {@link AudioTrack}'s JSON.
     *
     * @param jsonObject JSON data that has been accumulated.
     * @param audioTrack the track that was loaded.
     * @return the new {@link JSONObject}
     */
    JSONObject modifyAudioTrackJson(JSONObject jsonObject, AudioTrack audioTrack);
}
