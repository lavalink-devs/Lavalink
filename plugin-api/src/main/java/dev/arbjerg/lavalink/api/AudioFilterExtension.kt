package dev.arbjerg.lavalink.api

import com.fasterxml.jackson.databind.JsonNode
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import kotlinx.serialization.json.JsonElement
import org.json.JSONObject

/**
 * This interface allows defining custom Lavaplayer audio filters. It is configured via the "filters" WebSocket op.
 * The extension is used when a "filters" operation is received which has a value equal to [.getName]
 */
@Suppress("DeprecatedCallableAddReplaceWith") // sadly we cannot provide auto conversion for this
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
    @Deprecated(
        """As of v3.7 Jackson is the preferred way of JSON serialization,
      use AudioFilterExtension#build(JsonElement, AudioDataFormat, FloatPcmAudioFilter) instead."""
    )
    fun build(data: JSONObject?, format: AudioDataFormat?, output: FloatPcmAudioFilter?): FloatPcmAudioFilter? = null

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     */
    @Deprecated(
        """as of v4.0.0 Kotlinx.serialization is used, implement
      AudioFilterExtension#build(JsonElement, AudioDataFormat, FloatPcmAudioFilter) instead"""
    )
    fun build(data: JsonNode, format: AudioDataFormat?, output: FloatPcmAudioFilter?): FloatPcmAudioFilter? =
        build(JSONObject(data.toString()), format, output)

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     */
    fun build(data: JsonElement, format: AudioDataFormat?, output: FloatPcmAudioFilter?): FloatPcmAudioFilter? =
        build(data.toJsonNode(), format, output)

    /**
     * Checks if this filter is enabled.
     *
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     */
    @Deprecated(
        """as of v4.0.0 Kotlinx.serialization is used, implement
      AudioFilterExtension#isEnabled(JsonElement) instead"""
    )
    fun isEnabled(data: JSONObject?): Boolean = false

    /**
     * Checks if this filter is enabled.
     *
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     */
    @Deprecated(
        """as of v4.0.0 Kotlinx.serialization is used, implement
      AudioFilterExtension#isEnabled(JsonElement) instead"""
    )
    fun isEnabled(data: JsonNode): Boolean = isEnabled(JSONObject(data.toString()))

    /**
     * Checks if this filter is enabled.
     *
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     */
    fun isEnabled(data: JsonElement): Boolean = isEnabled(data.toJsonNode())
}
