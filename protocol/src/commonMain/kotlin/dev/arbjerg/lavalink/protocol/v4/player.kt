package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.jvm.JvmInline

@Serializable()
@JvmInline
value class Players(val players: List<Player>)

@Serializable
data class Player(
    val guildId: String,
    val track: Track?,
    val volume: Int,
    val paused: Boolean,
    val state: PlayerState,
    val voice: VoiceState,
    val filters: Filters
)

@Serializable
data class Track(
    val encoded: String,
    val info: TrackInfo,
    val pluginInfo: JsonObject,
    val userData: JsonObject
) : LoadResult.Data

@Serializable
@JvmInline
value class Tracks(val tracks: List<Track>)

@Serializable
@JvmInline
value class EncodedTracks(val tracks: ArrayList<String>)

@Serializable
data class TrackInfo(
    val identifier: String,
    val isSeekable: Boolean,
    val author: String,
    val length: Long,
    val isStream: Boolean,
    val position: Long,
    val title: String,
    val uri: String?,
    val sourceName: String,
    val artworkUrl: String?,
    val isrc: String?
)

@Serializable
data class VoiceState(
    val token: String,
    val endpoint: String,
    val sessionId: String
)

@Serializable
data class PlayerState(
    val time: Long,
    val position: Long,
    val connected: Boolean,
    val ping: Long
)

@Serializable
data class PlayerUpdateTrack(
    val encoded: Omissible<String?> = Omissible.Omitted(),
    val identifier: Omissible<String> = Omissible.Omitted(),
    val userData: Omissible<JsonObject> = Omissible.Omitted()
)

@Serializable
data class PlayerUpdate(
    @Deprecated("Use PlayerUpdateTrack#encoded instead", ReplaceWith("encoded"))
    val encodedTrack: Omissible<String?> = Omissible.Omitted(),
    @Deprecated("Use PlayerUpdateTrack#identifier instead")
    val identifier: Omissible<String> = Omissible.Omitted(),
    val track: Omissible<PlayerUpdateTrack> = Omissible.Omitted(),
    val position: Omissible<Long> = Omissible.Omitted(),
    val endTime: Omissible<Long?> = Omissible.Omitted(),
    val volume: Omissible<Int> = Omissible.Omitted(),
    val paused: Omissible<Boolean> = Omissible.Omitted(),
    val filters: Omissible<Filters> = Omissible.Omitted(),
    val voice: Omissible<VoiceState> = Omissible.Omitted()
)
