package lavalink.server.player.filters

import com.google.gson.Gson
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FilterChainBuilder
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class FilterChain : PcmFilterFactory {

    companion object {
        private val gson = Gson()
        fun parse(json: String) = gson.fromJson(json, FilterChain::class.java)!!
    }

    var volume: Float? = null
    var equalizer: List<Band>? = null
    private val karaoke: KaraokeConfig? = null
    private val timescale: TimescaleConfig? = null
    private val tremolo: TremoloConfig? = null
    private val vibrato: VibratoConfig? = null

    override fun buildChain(track: AudioTrack, format: AudioDataFormat, output: UniversalPcmAudioFilter): MutableList<AudioFilter> {
        val list = listOfNotNull(
                volume?.let { VolumeConfig(it) },
                equalizer?.let { EqualizerConfig(it) },
                karaoke,
                timescale,
                tremolo,
                vibrato
        )
        val builder = FilterChainBuilder()
        builder.addFirst(output)
        list.filter { it.isEnabled }
                .map { it.build(format, builder.makeFirstFloat(format.channelCount)) }
                .forEach { builder.addFirst(it) }

        return builder.build(null, format.channelCount)
                .filters.apply { dropLast(1) }
    }
}