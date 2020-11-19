package lavalink.server.player.filters

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter
import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class VolumeConfig(private var volume: Float) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VolumePcmAudioFilter(output, format.channelCount).also {
            it.volume = volume
        }
    }

    override val isEnabled: Boolean get() = volume != 1.0f
}

class EqualizerConfig(bands: List<Band>) : FilterConfig() {
    private val array = FloatArray(Equalizer.BAND_COUNT) { 0.0f }

    init {
        bands.forEach { array[it.band] = it.gain }
    }

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
            Equalizer(format.channelCount, output, array)

    override val isEnabled: Boolean get() = array.any { it != 0.0f }
}

data class Band(val band: Int, val gain: Float)

class KaraokeConfig(
        private val level: Float = 1.0f,
        private val monoLevel: Float = 1.0f,
        private val filterBand: Float = 220.0f,
        private val filterWidth: Float = 100.0f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate)
                .setLevel(level)
                .setMonoLevel(monoLevel)
                .setFilterBand(filterBand)
                .setFilterWidth(filterWidth)
    }
    override val isEnabled: Boolean get() = true
}

class TimescaleConfig(
        private val speed: Double = 1.0,
        private val pitch: Double = 1.0,
        private val rate: Double = 1.0
) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate)
                .setSpeed(speed)
                .setPitch(pitch)
                .setRate(rate)
    }
    override val isEnabled: Boolean get() = speed != 1.0 || pitch != 1.0 || rate != 1.0

}

class TremoloConfig(
        private val frequency: Float = 2.0f,
        private val depth: Float = 0.5f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TremoloPcmAudioFilter(output, format.channelCount, format.sampleRate)
                .setFrequency(frequency)
                .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f
}

class VibratoConfig(
        private val frequency: Float = 2.0f,
        private val depth: Float = 0.5f
) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate)
                .setFrequency(frequency)
                .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f

}

abstract class FilterConfig {
    abstract fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter
    abstract val isEnabled: Boolean
}