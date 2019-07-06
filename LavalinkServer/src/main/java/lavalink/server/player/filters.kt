package lavalink.server.player

import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.*
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.json.JSONObject

class FilterChain : PcmFilterFactory {

    private var equalizer: EqualizerConfig? = null
    private var karaoke: KaraokeConfig? = null
    private var timescale: TimescaleConfig? = null
    private var tremolo: TremoloConfig? = null
    private var vibrato: VibratoConfig? = null
    var volume: VolumeConfig? = null

    fun parse(json: JSONObject) {
        // TODO
    }

    override fun buildChain(track: AudioTrack, format: AudioDataFormat, output: UniversalPcmAudioFilter): MutableList<AudioFilter> {
        val list = listOfNotNull(equalizer, karaoke, timescale, tremolo, vibrato, volume)
        val builder = FilterChainBuilder()
        builder.addFirst(output)
        list.filter { it.isEnabled }
                .map { it.build(format, builder.makeFirstFloat(format.channelCount)) }
                .forEach { builder.addFirst(it) }

        return builder.build(null, format.channelCount)
                .filters.apply { dropLast(1) }
    }
}

class EqualizerConfig(json: JSONObject) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class KaraokeConfig(json: JSONObject) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class TimescaleConfig(json: JSONObject) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class TremoloConfig(json: JSONObject) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class VibratoConfig(json: JSONObject) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class VolumeConfig(var volume: Float = 1.0f) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        return VolumePcmAudioFilter(output, format.channelCount).also {
            it.volume = volume
        }
    }

    override val isEnabled: Boolean get() = volume != 1.0f
}

abstract class FilterConfig {
    abstract fun build(format: AudioDataFormat, output: FloatPcmAudioFilter) : AudioFilter
    abstract val isEnabled: Boolean
}