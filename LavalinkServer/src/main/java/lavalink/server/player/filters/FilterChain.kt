package lavalink.server.player.filters

import com.google.gson.Gson
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioFilterExtension
import org.json.JSONObject

class FilterChain : PcmFilterFactory {

    companion object {
        private val gson = Gson()

        fun parse(json: JSONObject, extensions: List<AudioFilterExtension>): FilterChain {
            return gson.fromJson(json.toString(), FilterChain::class.java)!!
                .apply { parsePluginConfigs(json, extensions) }
        }
    }

    var volume: Float? = null
    var equalizer: List<Band>? = null
    private val karaoke: KaraokeConfig? = null
    private val timescale: TimescaleConfig? = null
    private val tremolo: TremoloConfig? = null
    private val vibrato: VibratoConfig? = null
    private val distortion: DistortionConfig? = null
    private val rotation: RotationConfig? = null
    private val channelMix: ChannelMixConfig? = null
    private val lowPass: LowPassConfig? = null
    @Transient
    private var pluginFilters: List<PluginConfig> = emptyList()

    private fun parsePluginConfigs(json: JSONObject, extensions: List<AudioFilterExtension>) {
        pluginFilters = extensions.mapNotNull {
            val obj = json.optJSONObject(it.name) ?: return@mapNotNull null
            PluginConfig(it, obj)
        }
    }

    private fun buildList() = listOfNotNull(
            volume?.let { VolumeConfig(it) },
            equalizer?.let { EqualizerConfig(it) },
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

    override fun buildChain(track: AudioTrack?, format: AudioDataFormat, output: UniversalPcmAudioFilter): MutableList<AudioFilter> {
        val enabledFilters = buildList().takeIf { it.isNotEmpty() }
            ?: return mutableListOf()

        val pipeline = mutableListOf<FloatPcmAudioFilter>()

        for (filter in enabledFilters) {
            val outputTo = pipeline.lastOrNull() ?: output
            pipeline.add(filter.build(format, outputTo))
        }

        return pipeline.reversed().toMutableList() // Output last
    }

    private class PluginConfig(val extension: AudioFilterExtension, val json: JSONObject) : FilterConfig() {
        override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
            extension.build(json, format, output)
        override val isEnabled = extension.isEnabled(json)
    }

}
