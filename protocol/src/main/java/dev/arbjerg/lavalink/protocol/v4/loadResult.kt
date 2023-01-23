package dev.arbjerg.lavalink.protocol.v4

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException


data class LoadResult(
    val loadType: ResultStatus,
    val tracks: List<Track>,
    val playlistInfo: PlaylistInfo?,
    val exception: Exception?
) {
    companion object {
        fun trackLoaded(track: Track) = LoadResult(ResultStatus.TRACK_LOADED, listOf(track), null, null)
        fun playlistLoaded(playlistInfo: PlaylistInfo, tracks: List<Track>) = LoadResult(
            ResultStatus.PLAYLIST_LOADED,
            tracks,
            playlistInfo,
            null
        )
        fun searchResultLoaded(tracks: List<Track>) = LoadResult(ResultStatus.SEARCH_RESULT, tracks, null, null)
        val noMatches = LoadResult(ResultStatus.NO_MATCHES, emptyList(), null, null)
        fun loadFailed(exception: FriendlyException) =
            LoadResult(ResultStatus.LOAD_FAILED, emptyList(), null, Exception.fromFriendlyException(exception))

    }
}

data class PlaylistInfo(
    val name: String,
    val selectedTrack: Int
)

data class Exception(
    val message: String?,
    val severity: FriendlyException.Severity,
    val cause: String
) {
    companion object {
        fun fromFriendlyException(e: FriendlyException) = Exception(
            e.message,
            e.severity,
            e.toString()
        )
    }
}

enum class ResultStatus {
    TRACK_LOADED,
    PLAYLIST_LOADED,
    SEARCH_RESULT,
    NO_MATCHES,
    LOAD_FAILED
}