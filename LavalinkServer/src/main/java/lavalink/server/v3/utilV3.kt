/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package lavalink.server.v3

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioFilterExtension
import dev.arbjerg.lavalink.protocol.v3.*
import dev.arbjerg.lavalink.protocol.v4.json
import kotlinx.serialization.decodeFromString
import lavalink.server.io.SocketContext
import lavalink.server.player.LavalinkPlayer
import lavalink.server.player.filters.*
import lavalink.server.player.filters.Band
import lavalink.server.util.toJsonNode
import dev.arbjerg.lavalink.protocol.v3.Band as BandV3

fun AudioTrack.toTrackV3(): Track {
    return this.toTrackV3(encodeTrack(this))
}

fun AudioTrack.toTrackV3(encoded: String): Track {
    return Track(encoded, encoded, this.toInfoV3())
}

fun AudioTrack.toInfoV3(): TrackInfo {
    return TrackInfo(
        this.identifier,
        this.isSeekable,
        this.info.author,
        this.info.length,
        this.info.isStream,
        this.position,
        this.info.title,
        this.info.uri,
        this.sourceManager.sourceName
    )
}

fun AudioPlaylist.toPlaylistInfoV3(): PlaylistInfo {
    return PlaylistInfo(this.name, this.tracks.indexOf(this.selectedTrack))
}

fun LavalinkPlayer.toPlayerV3(context: SocketContext): Player {
    val connection = context.getMediaConnection(this).gatewayConnection
    val voiceServerInfo = context.koe.getConnection(guildId)?.voiceServerInfo

    return Player(
        guildId.toString(),
        track?.toTrackV3(),
        audioPlayer.volume,
        audioPlayer.isPaused,
        VoiceState(
            voiceServerInfo?.token ?: "",
            voiceServerInfo?.endpoint ?: "",
            voiceServerInfo?.sessionId ?: "",
            connection?.isOpen ?: false,
            connection?.ping ?: -1
        ),
        filters.toFiltersV3(),
    )
}

fun FilterChain.Companion.parseV3(
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
        parsePluginConfigs(filters.pluginFilters.mapValues { (_, value) ->
            json.decodeFromString(value.toString())
        }, extensions)
    }
}

fun FilterChain.toFiltersV3(): Filters {
    return Filters(
        volume?.volume,
        equalizer?.bands?.map { BandV3(it.band, it.gain) },
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
        pluginFilters.associate { it.extension.name to it.json.toJsonNode() }
    )
}
