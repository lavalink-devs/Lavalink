package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LoadResult(
    val loadType: ResultStatus,
    val tracks: List<Track>,
    val playlistInfo: PlaylistInfo?,
    val pluginInfo: JsonObject?,
    val exception: Exception?
) {
    companion object {
        fun trackLoaded(track: Track) = LoadResult(ResultStatus.TRACK_LOADED, listOf(track), null, null, null)
        fun playlistLoaded(playlistInfo: PlaylistInfo, pluginInfo: JsonObject, tracks: List<Track>) = LoadResult(
            ResultStatus.PLAYLIST_LOADED,
            tracks,
            playlistInfo,
            pluginInfo,
            null
        )

        fun searchResult(tracks: List<Track>) = LoadResult(ResultStatus.SEARCH_RESULT, tracks, null, null, null)
        val noMatches = LoadResult(ResultStatus.NO_MATCHES, emptyList(), null, null, null)
        fun loadFailed(exception: Exception) =
            LoadResult(ResultStatus.LOAD_FAILED, emptyList(), null, null, exception)

    }
}

@Serializable
data class PlaylistInfo(
    val name: String,
    val selectedTrack: Int
)

@Serializable
data class Playlist(
    val info: PlaylistInfo,
    val pluginInfo: JsonObject,
    val tracks: List<Track>
)

@Serializable
data class Exception(
    val message: String?,
    val severity: Severity,
    val cause: String
) {

    /**
     * Severity levels for FriendlyException
     */
    @Serializable
    enum class Severity {
        /**
         * The cause is known and expected, indicates that there is nothing wrong with the library itself.
         */
        COMMON,

        /**
         * The cause might not be exactly known, but is possibly caused by outside factors. For example when an outside
         * service responds in a format that we do not expect.
         */
        SUSPICIOUS,

        /**
         * If the probable cause is an issue with the library or when there is no way to tell what the cause might be.
         * This is the default level and other levels are used in cases where the thrower has more in-depth knowledge
         * about the error.
         */
        FAULT;

        companion object
    }

    companion object
}

@Serializable
enum class ResultStatus {
    TRACK_LOADED,
    PLAYLIST_LOADED,
    SEARCH_RESULT,
    NO_MATCHES,
    LOAD_FAILED
}
