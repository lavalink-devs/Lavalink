package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import kotlinx.serialization.json.JsonObject;

public interface AudioPluginInfoModifier {

    /**
     * Adds custom fields to an {@link AudioTrack}'s JSON.
     *
     * @param track the track that was loaded.
     * @param node  the info node which can be altered.
     */
    default void modifyAudioTrackPluginInfo(AudioTrack track, JsonObject node) {
    }

    /**
     * Adds custom fields to an {@link AudioPlaylist}'s JSON.
     *
     * @param playlist the playlist that was loaded.
     * @param node     the info node which can be altered.
     */
    default void modifyAudioPlaylistPluginInfo(AudioPlaylist playlist, JsonObject node) {
    }

}
