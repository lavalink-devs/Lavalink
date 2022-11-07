package dev.arbjerg.lavalink.protocol

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Filters(
    var volume: Float? = null,
    var equalizer: List<Band>? = null,
    val karaoke: Karaoke? = null,
    val timescale: Timescale? = null,
    val tremolo: Tremolo? = null,
    val vibrato: Vibrato? = null,
    val distortion: Distortion? = null,
    val rotation: Rotation? = null,
    val channelMix: ChannelMix? = null,
    val lowPass: LowPass? = null,

    @JsonAnyGetter
    @JsonAnySetter
    @get:JsonIgnore
    val pluginFilters: Map<String, JsonNode> = mutableMapOf()
)

data class Band(
    val band: Int,
    val gain: Float = 1.0f
)

open class Karaoke(
    val level: Float = 1.0f,
    val monoLevel: Float = 1.0f,
    val filterBand: Float = 220.0f,
    val filterWidth: Float = 100.0f
)

open class Timescale(
    val speed: Double = 1.0,
    val pitch: Double = 1.0,
    val rate: Double = 1.0
)

open class Tremolo(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

open class Vibrato(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

open class Distortion(
    val sinOffset: Float = 0.0f,
    val sinScale: Float = 1.0f,
    val cosOffset: Float = 0.0f,
    val cosScale: Float = 1.0f,
    val tanOffset: Float = 0.0f,
    val tanScale: Float = 1.0f,
    val offset: Float = 0.0f,
    val scale: Float = 1.0f
)

open class Rotation(
    val rotationHz: Double = 0.0
)

open class ChannelMix(
    val leftToLeft: Float = 1.0f,
    val leftToRight: Float = 0.0f,
    val rightToLeft: Float = 0.0f,
    val rightToRight: Float = 1.0f

)

open class LowPass(
    val smoothing: Float = 20.0f
)  