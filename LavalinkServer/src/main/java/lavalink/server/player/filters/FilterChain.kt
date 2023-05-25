package lavalink.server.player.filters

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.v4.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
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
                filters.volume.ifPresent(::VolumeConfig),
                filters.equalizer.ifPresentAndNotNull {
                    EqualizerConfig(it.map { band -> Band(band.band, band.gain) })
                },
                filters.karaoke.ifPresentAndNotNull { KaraokeConfig(it.level, it.monoLevel, it.filterBand, it.filterWidth) },
                filters.timescale.ifPresentAndNotNull { TimescaleConfig(it.speed, it.pitch, it.rate) },
                filters.tremolo.ifPresentAndNotNull { TremoloConfig(it.frequency, it.depth) },
                filters.vibrato.ifPresentAndNotNull { VibratoConfig(it.frequency, it.depth) },
                filters.distortion.ifPresentAndNotNull {
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
                filters.rotation.ifPresentAndNotNull { RotationConfig(it.rotationHz) },
                filters.channelMix.ifPresentAndNotNull {
                    ChannelMixConfig(
                        it.leftToLeft,
                        it.leftToRight,
                        it.rightToLeft,
                        it.rightToRight
                    )
                },
                filters.lowPass.ifPresentAndNotNull { LowPassConfig(it.smoothing) },
            ).apply {
                parsePluginConfigs(filters.pluginFilters, extensions)
            }
        }
    }

    fun parsePluginConfigs(dynamicValues: Map<String, JsonElement>, extensions: List<AudioFilterExtension>) {
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
            val builtFilter = filter.build(format, outputTo)
            if (builtFilter != null) {
                pipeline.add(builtFilter)
            }
        }

        return pipeline.reversed().toMutableList() // Output last
    }

    fun toFilters(): Filters {
        return Filters(
            volume?.volume.toOmissible(),
            equalizer?.bands?.map { Bandv4(it.band, it.gain) }.toOmissible(),
            karaoke?.let { Karaoke(it.level, it.monoLevel, it.filterBand, it.filterWidth) }.toOmissible(),
            timescale?.let { Timescale(it.speed, it.pitch, it.rate) }.toOmissible(),
            tremolo?.let { Tremolo(it.frequency, it.depth) }.toOmissible(),
            vibrato?.let { Vibrato(it.frequency, it.depth) }.toOmissible(),
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
            }.toOmissible(),
            rotation?.let { Rotation(it.rotationHz) }.toOmissible(),
            channelMix?.let { ChannelMix(it.leftToLeft, it.leftToRight, it.rightToLeft, it.rightToRight) }
                .toOmissible(),
            lowPass?.let { LowPass(it.smoothing) }.toOmissible(),
            pluginFilters.associate { it.extension.name to it.json }
        )
    }

    class PluginConfig(val extension: AudioFilterExtension, val json: JsonElement) : FilterConfig() {
        override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter? =
            extension.build(json, format, output)

        override val isEnabled = extension.isEnabled(json)
        override val name: String = extension.name
    }

}
