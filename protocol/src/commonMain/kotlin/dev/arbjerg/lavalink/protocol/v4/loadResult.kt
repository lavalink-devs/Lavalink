@file:Suppress("DataClassPrivateConstructor")

package dev.arbjerg.lavalink.protocol.v4

import dev.arbjerg.lavalink.protocol.v4.serialization.asPolymorphicDeserializer
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(with = LoadResult.Serializer::class)
sealed interface LoadResult {
    val loadType: ResultStatus
    val data: Data?

    sealed interface Data

    interface HasTracks {
        val tracks: List<Track>
    }

    @Serializable
    data class TrackLoaded private constructor(
        override val loadType: ResultStatus,
        override val data: Track
    ) : LoadResult {
        constructor(data: Track) : this(ResultStatus.TRACK, data)
    }

    @Serializable
    data class PlaylistLoaded private constructor(
        override val loadType: ResultStatus,
        override val data: Playlist
    ) : LoadResult {
        constructor(data: Playlist) : this(ResultStatus.PLAYLIST, data)

    }

    @Serializable
    data class SearchResult private constructor(
        override val loadType: ResultStatus,
        override val data: Data
    ) : LoadResult {
        constructor(data: Data) : this(ResultStatus.SEARCH, data)

        @Serializable(with = Data.Serializer::class)
        data class Data(override val tracks: List<Track>) : HasTracks, LoadResult.Data {
            object Serializer : KSerializer<Data> {
                private val delegate = ListSerializer(Track.serializer())
                override val descriptor: SerialDescriptor = delegate.descriptor

                override fun deserialize(decoder: Decoder): Data =
                    Data(decoder.decodeInline(descriptor).decodeSerializableValue(delegate))

                override fun serialize(encoder: Encoder, value: Data) {
                    encoder.encodeInline(descriptor).encodeSerializableValue(delegate, value.tracks)
                }
            }
        }
    }

    @Serializable
    open class NoMatches private constructor(override val loadType: ResultStatus, override val data: Data?) :
        LoadResult {
        companion object {
            private val instance: NoMatches = NoMatches(ResultStatus.NONE, null)
            operator fun invoke() = instance
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is NoMatches) return false

            return loadType == other.loadType
        }

        override fun hashCode(): Int = loadType.hashCode()

        override fun toString(): String = "NoMatches"

    }

    @Serializable
    data class LoadFailed private constructor(override val loadType: ResultStatus, override val data: Exception) :
        LoadResult {
        constructor(data: Exception) : this(ResultStatus.ERROR, data)
    }

    companion object {
        fun trackLoaded(track: Track) = TrackLoaded(track)

        fun playlistLoaded(playlistInfo: PlaylistInfo, pluginInfo: JsonObject, tracks: List<Track>) =
            PlaylistLoaded(
                Playlist(
                    playlistInfo,
                    pluginInfo,
                    tracks
                )
            )

        fun searchResult(tracks: List<Track>) = SearchResult(SearchResult.Data(tracks))
        fun loadFailed(exception: Exception) = LoadFailed(exception)

    }

    object Serializer : JsonContentPolymorphicSerializer<LoadResult>(LoadResult::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<LoadResult> =
            element.asPolymorphicDeserializer<ResultStatus, _>(descriptor, "loadType") {
                when (it) {
                    ResultStatus.TRACK -> TrackLoaded.serializer()
                    ResultStatus.PLAYLIST -> PlaylistLoaded.serializer()
                    ResultStatus.SEARCH -> SearchResult.serializer()
                    ResultStatus.NONE -> NoMatches.serializer()
                    ResultStatus.ERROR -> LoadFailed.serializer()
                }
            }
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
    @EncodeDefault
    val pluginInfo: JsonObject = JsonObject(emptyMap()),
    val tracks: List<Track>
) : LoadResult.Data {

    /**
     * Deserialize the plugin info into a specific type.
     * This method is a convenience method meant to be used in Java,
     * since Kotlin extension methods are painful to use in Java.
     *
     * @param deserializer The deserializer to use. (e.g. `T.Companion.serializer()`)
     *
     * @return the deserialized plugin info as type T
     */
    fun <T> deserializePluginInfo(deserializer: DeserializationStrategy<T>): T = pluginInfo.deserialize(deserializer)
}

@Serializable
data class Exception(
    val message: String?,
    val severity: Severity,
    val cause: String
) : LoadResult.Data {

    /**
     * Severity levels for FriendlyException
     */
    @Serializable
    enum class Severity {
        /**
         * The cause is known and expected, indicates that there is nothing wrong with the library itself.
         */
        @SerialName("common")
        COMMON,

        /**
         * The cause might not be exactly known, but is possibly caused by outside factors. For example when an outside
         * service responds in a format that we do not expect.
         */
        @SerialName("suspicious")
        SUSPICIOUS,

        /**
         * If the probable cause is an issue with the library or when there is no way to tell what the cause might be.
         * This is the default level and other levels are used in cases where the thrower has more in-depth knowledge
         * about the error.
         */
        @SerialName("fault")
        FAULT;

        companion object
    }

    companion object
}

@Serializable
enum class ResultStatus {
    @SerialName("track")
    TRACK,

    @SerialName("playlist")
    PLAYLIST,

    @SerialName("search")
    SEARCH,

    @SerialName("empty")
    NONE,

    @SerialName("error")
    ERROR
}
