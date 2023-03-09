package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlinx.serialization.json.JsonObject

interface AudioPluginInfoModifier {
    /**
     * Adds custom fields to an [AudioTrack]'s JSON.
     *
     * @param track the track that was loaded.
     * @return an [JsonObject] containing customized info
     */
    fun modifyAudioTrackPluginInfo(track: AudioTrack): JsonObject? {
        return null
    }

    /**
     * Adds custom fields to an [AudioPlaylist]'s JSON.
     *
     * @param playlist the playlist that was loaded.
     * @return an [JsonObject] containing customized info
     */
    fun modifyAudioPlaylistPluginInfo(playlist: AudioPlaylist): JsonObject? {
        return null
    }
}
