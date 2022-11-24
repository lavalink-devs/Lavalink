package dev.arbjerg.lavalink.protocol

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException


data class LoadResult(
    val loadType: ResultStatus,
    val tracks: List<Track>,
    val playlistInfo: PlaylistInfo?,
    val exception: Exception?
) {

    constructor(
        loadResultType: ResultStatus,
        tracks: List<Track>,
        playlistInfo: PlaylistInfo?,
    ) : this(
        loadResultType, tracks, playlistInfo, null
    )

    constructor(exception: Exception?) : this(ResultStatus.LOAD_FAILED, emptyList(), null, exception)
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
    constructor(e: FriendlyException) : this(e.message, e.severity, e.toString())
}

enum class ResultStatus {
    TRACK_LOADED,
    PLAYLIST_LOADED,
    SEARCH_RESULT,
    NO_MATCHES,
    LOAD_FAILED
}