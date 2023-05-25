package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import kotlinx.serialization.json.JsonElement

/**
 * This interface allows defining custom Lavaplayer audio filters. It is configured via the "filters" WebSocket op.
 * The extension is used when a "filters" operation is received which has a value equal to [.getName]
 */
interface AudioFilterExtension {
    /**
     * The key of the filter.
     */
    val name: String

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     */
    fun build(data: JsonElement, format: AudioDataFormat?, output: FloatPcmAudioFilter?): FloatPcmAudioFilter? = null

    /**
     * Checks if this filter is enabled.
     *
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     */
    fun isEnabled(data: JsonElement): Boolean = false
}
