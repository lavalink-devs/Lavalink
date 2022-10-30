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
import dev.arbjerg.lavalink.protocol.*
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer as LavaplayerEqualizer

class VolumeConfig(val volume: Float) : FilterConfig {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VolumePcmAudioFilter(output).also {
            it.volume = volume
        }
    }

    override val isEnabled: Boolean get() = volume != 1.0f
}

class EqualizerConfig(val bands: List<Band>) : FilterConfig {
    private val array = FloatArray(LavaplayerEqualizer.BAND_COUNT) { 0.0f }

    init {
        bands.forEach { array[it.band] = it.gain }
    }

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter =
        LavaplayerEqualizer(format.channelCount, output, array)

    override val isEnabled: Boolean get() = array.any { it != 0.0f }
}

class KaraokeConfig(
    karaoke: Karaoke
) : Karaoke(karaoke.level, karaoke.monoLevel, karaoke.filterBand, karaoke.filterWidth), FilterConfig {
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
    timescale: Timescale
) : Timescale(timescale.speed, timescale.pitch, timescale.rate), FilterConfig {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setSpeed(speed)
            .setPitch(pitch)
            .setRate(rate)
    }

    override val isEnabled: Boolean get() = speed != 1.0 || pitch != 1.0 || rate != 1.0

}

class TremoloConfig(
    tremolo: Tremolo
) : Tremolo(tremolo.frequency, tremolo.depth), FilterConfig {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return TremoloPcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setFrequency(frequency)
            .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f
}

class VibratoConfig(
    vibrato: Vibrato
) : Vibrato(vibrato.frequency, vibrato.depth), FilterConfig {

    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate)
            .setFrequency(frequency)
            .setDepth(depth)
    }

    override val isEnabled: Boolean get() = depth != 0.0f

}

class DistortionConfig(
    distortion: Distortion
) : Distortion(
    distortion.sinOffset,
    distortion.sinScale,
    distortion.cosOffset,
    distortion.cosScale,
    distortion.tanOffset,
    distortion.tanScale,
    distortion.offset,
    distortion.scale
), FilterConfig {

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

}

class RotationConfig(
    rotation: Rotation
) : Rotation(rotation.rotationHz), FilterConfig {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return RotationPcmAudioFilter(output, format.sampleRate)
            .setRotationSpeed(rotationHz)
    }

    override val isEnabled: Boolean get() = rotationHz != 0.0
}

class ChannelMixConfig(
    channelMix: ChannelMix
) : ChannelMix(channelMix.leftToLeft, channelMix.leftToRight, channelMix.rightToLeft, channelMix.rightToRight),
    FilterConfig {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return ChannelMixPcmAudioFilter(output)
            .setLeftToLeft(leftToLeft)
            .setLeftToRight(leftToRight)
            .setRightToLeft(rightToLeft)
            .setRightToRight(rightToRight)
    }

    override val isEnabled: Boolean get() = leftToLeft != 1f || leftToRight != 0f || rightToLeft != 0f || rightToRight != 1f
}

class LowPassConfig(
    lowPass: LowPass
) : LowPass(lowPass.smoothing), FilterConfig {
    override fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter {
        return LowPassPcmAudioFilter(output, format.channelCount)
            .setSmoothing(smoothing)
    }

    override val isEnabled: Boolean get() = smoothing > 1.0f
}

interface FilterConfig {
    fun build(format: AudioDataFormat, output: FloatPcmAudioFilter): FloatPcmAudioFilter
    @get:JsonIgnore
    val isEnabled: Boolean
}