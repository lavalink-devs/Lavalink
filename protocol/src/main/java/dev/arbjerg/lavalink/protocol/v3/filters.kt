package dev.arbjerg.lavalink.protocol.v3

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Filters(
    val volume: Float? = null,
    val equalizer: List<Band>? = null,
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
) {
    fun validate(disabledFilters: List<String>): List<String> {
        val filters = mutableListOf<String>()
        if ("volume" in disabledFilters && volume != null) {
            filters.add("volume")
        }
        if ("equalizer" in disabledFilters && equalizer != null) {
            filters.add("equalizer")
        }
        if ("karaoke" in disabledFilters && karaoke != null) {
            filters.add("karaoke")
        }
        if ("timescale" in disabledFilters && timescale != null) {
            filters.add("timescale")
        }
        if ("tremolo" in disabledFilters && tremolo != null) {
            filters.add("tremolo")
        }
        if ("vibrato" in disabledFilters && vibrato != null) {
            filters.add("vibrato")
        }
        if ("distortion" in disabledFilters && distortion != null) {
            filters.add("distortion")
        }
        if ("rotation" in disabledFilters && rotation != null) {
            filters.add("rotation")
        }
        if ("channelMix" in disabledFilters && channelMix != null) {
            filters.add("channelMix")
        }
        if ("lowPass" in disabledFilters && lowPass != null) {
            filters.add("lowPass")
        }
        for (filter in pluginFilters) {
            if (filter.key in disabledFilters) {
                filters.add(filter.key)
            }
        }
        return filters
    }
}

data class Band(
    val band: Int,
    val gain: Float = 1.0f
)

data class Karaoke(
    val level: Float = 1.0f,
    val monoLevel: Float = 1.0f,
    val filterBand: Float = 220.0f,
    val filterWidth: Float = 100.0f
)

data class Timescale(
    val speed: Double = 1.0,
    val pitch: Double = 1.0,
    val rate: Double = 1.0
)

data class Tremolo(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

data class Vibrato(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

data class Distortion(
    val sinOffset: Float = 0.0f,
    val sinScale: Float = 1.0f,
    val cosOffset: Float = 0.0f,
    val cosScale: Float = 1.0f,
    val tanOffset: Float = 0.0f,
    val tanScale: Float = 1.0f,
    val offset: Float = 0.0f,
    val scale: Float = 1.0f
)

data class Rotation(
    val rotationHz: Double = 0.0
)

data class ChannelMix(
    val leftToLeft: Float = 1.0f,
    val leftToRight: Float = 0.0f,
    val rightToLeft: Float = 0.0f,
    val rightToRight: Float = 1.0f
)

data class LowPass(
    val smoothing: Float = 20.0f
)  