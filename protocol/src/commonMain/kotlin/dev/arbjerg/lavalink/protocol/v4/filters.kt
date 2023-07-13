package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Filters(
    val volume: Omissible<Float> = Omissible.Omitted(),
    val equalizer: Omissible<List<Band>> = Omissible.Omitted(),
    val karaoke: Omissible<Karaoke?> = Omissible.Omitted(),
    val timescale: Omissible<Timescale?> = Omissible.Omitted(),
    val tremolo: Omissible<Tremolo?> = Omissible.Omitted(),
    val vibrato: Omissible<Vibrato?> = Omissible.Omitted(),
    val distortion: Omissible<Distortion?> = Omissible.Omitted(),
    val rotation: Omissible<Rotation?> = Omissible.Omitted(),
    val channelMix: Omissible<ChannelMix?> = Omissible.Omitted(),
    val lowPass: Omissible<LowPass?> = Omissible.Omitted(),
    val pluginFilters: Map<String, JsonElement> = mutableMapOf()
) {
    fun validate(disabledFilters: List<String>): List<String> = buildList {
        if ("volume" in disabledFilters && volume is Omissible.Present) {
            add("volume")
        }
        if ("equalizer" in disabledFilters && equalizer is Omissible.Present) {
            add("equalizer")
        }
        if ("karaoke" in disabledFilters && karaoke is Omissible.Present) {
            add("karaoke")
        }
        if ("timescale" in disabledFilters && timescale is Omissible.Present) {
            add("timescale")
        }
        if ("tremolo" in disabledFilters && tremolo is Omissible.Present) {
            add("tremolo")
        }
        if ("vibrato" in disabledFilters && vibrato is Omissible.Present) {
            add("vibrato")
        }
        if ("distortion" in disabledFilters && distortion is Omissible.Present) {
            add("distortion")
        }
        if ("rotation" in disabledFilters && rotation is Omissible.Present) {
            add("rotation")
        }
        if ("channelMix" in disabledFilters && channelMix is Omissible.Present) {
            add("channelMix")
        }
        if ("lowPass" in disabledFilters && lowPass is Omissible.Present) {
            add("lowPass")
        }
        for (filter in pluginFilters) {
            if (filter.key in disabledFilters) {
                add(filter.key)
            }
        }
    }
}

@Serializable
data class Band(
    val band: Int,
    val gain: Float = 1.0f
)

@Serializable
data class Karaoke(
    val level: Float = 1.0f,
    val monoLevel: Float = 1.0f,
    val filterBand: Float = 220.0f,
    val filterWidth: Float = 100.0f
)

@Serializable
data class Timescale(
    val speed: Double = 1.0,
    val pitch: Double = 1.0,
    val rate: Double = 1.0
)

@Serializable
data class Tremolo(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

@Serializable
data class Vibrato(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
)

@Serializable
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

@Serializable
data class Rotation(
    val rotationHz: Double = 0.0
)

@Serializable
data class ChannelMix(
    val leftToLeft: Float = 1.0f,
    val leftToRight: Float = 0.0f,
    val rightToLeft: Float = 0.0f,
    val rightToRight: Float = 1.0f

)

@Serializable
data class LowPass(
    val smoothing: Float = 20.0f
)
