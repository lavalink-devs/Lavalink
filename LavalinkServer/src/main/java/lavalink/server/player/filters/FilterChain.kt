package lavalink.server.player.filters

import com.fasterxml.jackson.databind.JsonNode
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.v4.*
import dev.arbjerg.lavalink.protocol.v4.Band as Bandv4

class FilterChain(
    val volume: VolumeConfig? = null,
    var equalizer: EqualizerConfig? = null,
    val karaoke: KaraokeConfig? = null,
    val timescale: TimescaleConfig? = null,
    val tremolo: TremoloConfig? = null,
    val vibrato: VibratoConfig? = null,
    val distortion: DistortionConfig? = null,
    val rotation: RotationConfig? = null,
    val channelMix: ChannelMixConfig? = null,
    val lowPass: LowPassConfig? = null,
) : PcmFilterFactory {

    @Volatile
    var pluginFilters: List<PluginConfig> = emptyList()

    companion object {
        fun parse(
            filters: Filters,
            extensions: List<AudioFilterExtension>,
        ): FilterChain {
            return FilterChain(
                filters.volume?.let { VolumeConfig(it) },
                filters.equalizer?.let {
                    EqualizerConfig(it.map { band -> Band(band.band, band.gain) })
                },
                filters.karaoke?.let { KaraokeConfig(it.level, it.monoLevel, it.filterBand, it.filterWidth) },
                filters.timescale?.let { TimescaleConfig(it.speed, it.pitch, it.rate) },
                filters.tremolo?.let { TremoloConfig(it.frequency, it.depth) },
                filters.vibrato?.let { VibratoConfig(it.frequency, it.depth) },
                filters.distortion?.let {
                    DistortionConfig(
                        it.sinOffset,
                        it.sinScale,
                        it.cosOffset,
                        it.cosScale,
                        it.tanOffset,
                        it.tanScale,
                        it.offset,
                        it.scale
                    )
                },
                filters.rotation?.let { RotationConfig(it.rotationHz) },
                filters.channelMix?.let {
                    ChannelMixConfig(
                        it.leftToLeft,
                        it.leftToRight,
                        it.rightToLeft,
                        it.rightToRight
                    )
                },
                filters.lowPass?.let { LowPassConfig(it.smoothing) },
            ).apply {
                parsePluginConfigs(filters.pluginFilters, extensions)
            }
        }
    }

    fun parsePluginConfigs(dynamicValues: Map<String, JsonNode>, extensions: List<AudioFilterExtension>) {
        pluginFilters = extensions.mapNotNull {
            val json = dynamicValues[it.name] ?: return@mapNotNull null
            PluginConfig(it, json)
        }
    }

    private fun buildList() = listOfNotNull(
        volume,
        equalizer,
        karaoke,
        timescale,
        tremolo,
        vibrato,
        distortion,
        rotation,
        channelMix,
        lowPass,
        *pluginFilters.toTypedArray()
    )

    val isEnabled get() = buildList().any { it.isEnabled }

    override fun buildChain(
        track: AudioTrack?,
        format: AudioDataFormat,
        output: UniversalPcmAudioFilter
    ): MutableList<AudioFilter> {
        val enabledFilters = buildList().takeIf { it.isNotEmpty() }
            ?: return mutableListOf()

        val pipeline = mutableListOf<FloatPcmAudioFilter>()

        for (filter in enabledFilters) {
            val outputTo = pipeline.lastOrNull() ?: output
            pipeline.add(filter.build(format, outputTo))
        }

        return pipeline.reversed().toMutableList() // Output last
    }

    fun toFilters(): Filters {
        return Filters(
            volume?.volume,
            equalizer?.bands?.map { Bandv4(it.band, it.gain) },
            karaoke?.let { Karaoke(it.level, it.monoLevel, it.filterBand, it.filterWidth) },
            timescale?.let { Timescale(it.speed, it.pitch, it.rate) },
            tremolo?.let { Tremolo(it.frequency, it.depth) },
            vibrato?.let { Vibrato(it.frequency, it.depth) },
            distortion?.let {
                Distortion(
                    it.sinOffset,
                    it.sinScale,
                    it.cosOffset,
                    it.cosScale,
                    it.tanOffset,
                    it.tanScale,
                    it.offset,
                    it.scale
                )
            },
            rotation?.let { Rotation(it.rotationHz) },
            channelMix?.let { ChannelMix(it.leftToLeft, it.leftToRight, it.rightToLeft, it.rightToRight) },
            lowPass?.let { LowPass(it.smoothing) },
            pluginFilters.associate { it.extension.name to it.json }
        )
    }

    class PluginConfig(val extension: AudioFilterExtension, val json: JsonNode) : FilterConfig() {
        override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
            extension.build(json, format, output)

        override val isEnabled = extension.isEnabled(json)
        override val name: String = extension.name
    }

}
