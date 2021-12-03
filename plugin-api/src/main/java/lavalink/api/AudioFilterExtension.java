package lavalink.api;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import org.json.JSONObject;

public interface AudioFilterExtension {
    String getName();
    FloatPcmAudioFilter build(JSONObject data, AudioDataFormat format, FloatPcmAudioFilter output);
}
