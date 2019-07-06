package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.filter.*
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.json.JSONObject

class FilterChain(
        var equalizer: EqualizerConfig? = null,
        var karaoke: KaraokeConfig? = null,
        var timescale: TimescaleConfig? = null,
        var tremolo: TremoloConfig? = null,
        var vibrato: VibratoConfig? = null,
        var volume: VolumeConfig? = null
) : PcmFilterFactory {

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

class EqualizerConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class KaraokeConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class TimescaleConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class TremoloConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class VibratoConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

class VolumeConfig(json: JSONObject) : FilterConfig(json) {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): AudioFilter {
        TODO("not implemented")
    }

    override val isEnabled: Boolean get() = false
}

abstract class FilterConfig(json: JSONObject) {
    abstract fun build(format: AudioDataFormat, output: FloatPcmAudioFilter) : AudioFilter
    abstract val isEnabled: Boolean
}