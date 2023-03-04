package dev.arbjerg.lavalink.protocol.v4

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode

data class Players(
    @JsonValue
    val players: List<Player>
)

data class Player(
    val guildId: String,
    val track: Track?,
    val volume: Int,
    val paused: Boolean,
    val voice: VoiceState,
    val filters: Filters
)

data class Track(
    val encoded: String,
    val info: TrackInfo,
    val pluginInfo: ObjectNode
)

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

data class VoiceState(
    val token: String = "",
    val endpoint: String = "",
    val sessionId: String = "",
    val connected: Boolean = false,
    val ping: Long = -1
)

data class PlayerState(
    val time: Long,
    val position: Long,
    val connected: Boolean,
    val ping: Long
)

@JsonDeserialize(using = PlayerUpdateDeserializer::class)
data class PlayerUpdate(
    val encodedTrack: Omissible<String?> = Omissible.omitted(),
    val identifier: Omissible<String> = Omissible.omitted(),
    val position: Omissible<Long> = Omissible.omitted(),
    val endTime: Omissible<Long?> = Omissible.omitted(),
    val volume: Omissible<Int> = Omissible.omitted(),
    val paused: Omissible<Boolean> = Omissible.omitted(),
    val filters: Omissible<Filters> = Omissible.omitted(),
    val voice: Omissible<VoiceState> = Omissible.omitted()
)

class PlayerUpdateDeserializer : JsonDeserializer<PlayerUpdate>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PlayerUpdate {
        val node = p.codec.readTree<JsonNode>(p)

        val encodedTrack = node.get("encodedTrack")?.let {
            if (it.isNull) Omissible.of<String?>(null) else Omissible.of(it.asText())
        } ?: Omissible.omitted()

        val identifier = node.get("identifier")?.let {
            Omissible.of(it.asText())
        } ?: Omissible.omitted()

        val position = node.get("position")?.let {
            Omissible.of(it.asLong())
        } ?: Omissible.omitted()

        val endTime = node.get("endTime")?.let {
            if (it.isNull) Omissible.of<Long?>(null) else Omissible.of<Long?>(it.asLong())
        } ?: Omissible.omitted()

        val volume = node.get("volume")?.let {
            Omissible.of(it.asInt())
        } ?: Omissible.omitted()

        val paused = node.get("paused")?.let {
            Omissible.of(it.asBoolean())
        } ?: Omissible.omitted()

        val filters = node.get("filters")?.let {
            Omissible.of(p.codec.treeToValue(it, Filters::class.java))
        } ?: Omissible.omitted()

        val voice = node.get("voice")?.let {
            Omissible.of(p.codec.treeToValue(it, VoiceState::class.java))
        } ?: Omissible.omitted()

        return PlayerUpdate(
            encodedTrack,
            identifier,
            position,
            endTime,
            volume,
            paused,
            filters,
            voice
        )
    }

}
