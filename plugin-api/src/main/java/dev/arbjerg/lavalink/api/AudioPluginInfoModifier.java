package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import kotlinx.serialization.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AudioPluginInfoModifier {

    /**
     * Adds custom fields to an {@link AudioTrack}'s JSON.
     *
     * @param track the track that was loaded.
     * @return an {@link JsonObject} containing customized info
     */
    @Nullable
    default JsonObject modifyAudioTrackPluginInfo(@NotNull AudioTrack track) {
        return null;
    }

    /**
     * Adds custom fields to an {@link AudioPlaylist}'s JSON.
     *
     * @param playlist the playlist that was loaded.
     * @return an {@link JsonObject} containing customized info
     */
    @Nullable
    default JsonObject modifyAudioPlaylistPluginInfo(@NotNull AudioPlaylist playlist) {
        return null;
    }
}
