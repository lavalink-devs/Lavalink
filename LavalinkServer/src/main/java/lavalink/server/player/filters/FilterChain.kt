package lavalink.server.player.filters

import com.fasterxml.jackson.databind.JsonNode
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.Filters

class FilterChain(
    private var volume: VolumeConfig? = null,
    var equalizer: EqualizerConfig? = null,
    private val karaoke: KaraokeConfig? = null,
    private val timescale: TimescaleConfig? = null,
    private val tremolo: TremoloConfig? = null,
    private val vibrato: VibratoConfig? = null,
    private val distortion: DistortionConfig? = null,
    private val rotation: RotationConfig? = null,
    private val channelMix: ChannelMixConfig? = null,
    private val lowPass: LowPassConfig? = null,
) : PcmFilterFactory {
    companion object {
        fun parse(
            filters: Filters,
            extensions: List<AudioFilterExtension>,
        ): FilterChain {
            return FilterChain(
                filters.volume?.let { VolumeConfig(it) },
                filters.equalizer?.let { EqualizerConfig(it) },
                filters.karaoke?.let { KaraokeConfig(it) },
                filters.timescale?.let { TimescaleConfig(it) },
                filters.tremolo?.let { TremoloConfig(it) },
                filters.vibrato?.let { VibratoConfig(it) },
                filters.distortion?.let { DistortionConfig(it) },
                filters.rotation?.let { RotationConfig(it) },
                filters.channelMix?.let { ChannelMixConfig(it) },
                filters.lowPass?.let { LowPassConfig(it) }
            ).apply {
                parsePluginConfigs(filters.pluginFilters, extensions)
            }
        }
    }


    @Transient
    private var pluginFilters: List<PluginConfig> = emptyList()

    private fun parsePluginConfigs(dynamicValues: Map<String, JsonNode>, extensions: List<AudioFilterExtension>) {
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
            equalizer?.bands,
            karaoke,
            timescale,
            tremolo,
            vibrato,
            distortion,
            rotation,
            channelMix,
            lowPass,
            pluginFilters.associate { it.extension.name to it.json }
        )
    }

    private class PluginConfig(val extension: AudioFilterExtension, val json: JsonNode) : FilterConfig {
        override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
            extension.build(json, format, output)

        override val isEnabled = extension.isEnabled(json)
        override val name: String = extension.name
    }

}
