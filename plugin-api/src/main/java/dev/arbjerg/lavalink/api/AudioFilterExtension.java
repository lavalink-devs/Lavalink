package dev.arbjerg.lavalink.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import kotlinx.serialization.json.JsonElement;
import org.json.JSONObject;

/**
 * This interface allows defining custom Lavaplayer audio filters. It is configured via the "filters" WebSocket op.
 * The extension is used when a "filters" operation is received which has a value equal to {@link #getName()}
 */
public interface AudioFilterExtension {
    /**
     * @return The key of the filter
     */
    String getName();

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     * @deprecated As of v3.7 Jackson is the preferred way of JSON serialization,
     * use {@link AudioFilterExtension#build(JsonElement, AudioDataFormat, FloatPcmAudioFilter)} instead.
     */
    @Deprecated
    default FloatPcmAudioFilter build(JSONObject data, AudioDataFormat format, FloatPcmAudioFilter output) {
        return null;
    }

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     * @deprecated as of v4.0.0 Kotlinx.serialization is used, implement
     * {@link AudioFilterExtension#build(JsonElement, AudioDataFormat, FloatPcmAudioFilter)} instead
     */
    @Deprecated
    default FloatPcmAudioFilter build(JsonNode data, AudioDataFormat format, FloatPcmAudioFilter output) {
        return build(new JSONObject(data.toString()), format, output);
    }

    /**
     * Builds a filter for a particular player.
     *
     * @param data   JSON data received from the client under the extension name key.
     * @param format format as specified by Lavaplayer.
     * @param output the output to be used by the produced filter.
     * @return a filter which produces the desired audio effect.
     */
    default FloatPcmAudioFilter build(JsonElement data, AudioDataFormat format, FloatPcmAudioFilter output) {
        return build(LegacySupport.toJsonNode(data), format, output);
    }

    /**
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     * @deprecated as of v4.0.0 Kotlinx.serialization is used, implement
     * {@link AudioFilterExtension#isEnabled(JsonElement)} instead
     */
    @Deprecated
    default boolean isEnabled(JSONObject data) {
        return false;
    }

    /**
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     * @deprecated as of v4.0.0 Kotlinx.serialization is used, implement
     * {@link AudioFilterExtension#isEnabled(JsonElement)} instead
     */
    @Deprecated
    default boolean isEnabled(JsonNode data) {
        return isEnabled(new JSONObject(data.toString()));
    }

    /**
     * @param data JSON data received from the client under the extension name key.
     * @return whether to build a filter. Returning false makes this extension do nothing.
     */
    default boolean isEnabled(JsonElement data) {
        return isEnabled(LegacySupport.toJsonNode(data));
    }
}
