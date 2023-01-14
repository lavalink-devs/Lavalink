package lavalink.server.player.filters

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.natanbc.lavadsp.channelmix.ChannelMixPcmAudioFilter
import com.github.natanbc.lavadsp.distortion.DistortionPcmAudioFilter
import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter
import com.github.natanbc.lavadsp.lowpass.LowPassPcmAudioFilter
import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter
import com.github.natanbc.lavadsp.volume.VolumePcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer as LavaplayerEqualizer

class VolumeConfig(val volume: Float) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VolumePcmAudioFilter(output).also {
            it.volume = volume
        }
    }

    override val isEnabled: Boolean get() = volume != 1.0f
    override val name: String get() = "volume"
}

data class Band(val band: Int, val gain: Float)

class EqualizerConfig(val bands: List<Band>) : FilterConfig() {
    private val array = FloatArray(LavaplayerEqualizer.BAND_COUNT) { 0.0f }

    init {
        bands.forEach { array[it.band] = it.gain }
    }

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
        LavaplayerEqualizer(format.channelCount, output, array)

    override val isEnabled: Boolean get() = array.any { it != 0.0f }
    override val name: String get() = "equalizer"
}

class KaraokeConfig(
    val level: Float = 1.0f,
    val monoLevel: Float = 1.0f,
    val filterBand: Float = 220.0f,
    val filterWidth: Float = 100.0f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setLevel(level)
            .setMonoLevel(monoLevel)
            .setFilterBand(filterBand)
            .setFilterWidth(filterWidth)
    }

    override val isEnabled: Boolean get() = true
    override val name: String get() = "karaoke"
}

class TimescaleConfig(
    val speed: Double = 1.0,
    val pitch: Double = 1.0,
    val rate: Double = 1.0
) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setSpeed(speed)
            .setPitch(pitch)
            .setRate(rate)
    }

    override val isEnabled: Boolean get() = speed != 1.0 || pitch != 1.0 || rate != 1.0
    override val name: String get() = "timescale"
}

class TremoloConfig(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TremoloPcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setFrequency(frequency)
            .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f
    override val name: String get() = "tremolo"
}

class VibratoConfig(
    val frequency: Float = 2.0f,
    val depth: Float = 0.5f
) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setFrequency(frequency)
            .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f
    override val name: String get() = "vibrato"
}

class DistortionConfig(
    val sinOffset: Float = 0.0f,
    val sinScale: Float = 1.0f,
    val cosOffset: Float = 0.0f,
    val cosScale: Float = 1.0f,
    val tanOffset: Float = 0.0f,
    val tanScale: Float = 1.0f,
    val offset: Float = 0.0f,
    val scale: Float = 1.0f
) : FilterConfig() {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return DistortionPcmAudioFilter(output, format.channelCount)
            .setSinOffset(sinOffset)
            .setSinScale(sinScale)
            .setCosOffset(cosOffset)
            .setCosScale(cosScale)
            .setTanOffset(tanOffset)
            .setTanScale(tanScale)
            .setOffset(offset)
            .setScale(scale)
    }

    override val isEnabled: Boolean get() = sinOffset != 0.0f || sinScale != 1.0f || cosOffset != 0.0f || cosScale != 1.0f || tanOffset != 0.0f || tanScale != 1.0f || offset != 0.0f || scale != 1.0f
    override val name: String get() = "distortion"
}

class RotationConfig(
    val rotationHz: Double = 0.0
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return RotationPcmAudioFilter(output, format.sampleRate)
            .setRotationSpeed(rotationHz)
    }

    override val isEnabled: Boolean get() = rotationHz != 0.0
    override val name: String get() = "rotation"
}

class ChannelMixConfig(
    val leftToLeft: Float = 1f,
    val leftToRight: Float = 0f,
    val rightToLeft: Float = 0f,
    val rightToRight: Float = 1f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return ChannelMixPcmAudioFilter(output)
            .setLeftToLeft(leftToLeft)
            .setLeftToRight(leftToRight)
            .setRightToLeft(rightToLeft)
            .setRightToRight(rightToRight)
    }

    override val isEnabled: Boolean get() = leftToLeft != 1f || leftToRight != 0f || rightToLeft != 0f || rightToRight != 1f
    override val name: String get() = "channelMix"
}

class LowPassConfig(
    val smoothing: Float = 20.0f
) : FilterConfig() {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return LowPassPcmAudioFilter(output, format.channelCount)
            .setSmoothing(smoothing)
    }

    override val isEnabled: Boolean get() = smoothing > 1.0f
    override val name: String get() = "lowPass"
}

abstract class FilterConfig {
    abstract fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter

    @get:JsonIgnore
    abstract val isEnabled: Boolean

    @get:JsonIgnore
    abstract val name: String
}