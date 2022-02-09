package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

internal data class LoadResult @JvmOverloads constructor(
    val loadResultType: ResultStatus,
    val tracks: List<AudioTrack>,
    val playlistName: String? = null,
    val selectedTrack: Int? = null,
    val playlist: AudioPlaylist? = null,
    val exception: FriendlyException? = null
) {
    constructor(exception: FriendlyException?) : this(
        loadResultType = ResultStatus.LOAD_FAILED,
        tracks = emptyList(),
        exception = exception
    )
}
