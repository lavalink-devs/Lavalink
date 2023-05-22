package lavalink.server.io

import com.fasterxml.jackson.databind.node.ObjectNode
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.springframework.stereotype.Component

@Component
class DefaultAudioPluginInfoModifier : AudioPluginInfoModifier {

    companion object {
        fun MediaContainerDescriptor.probeInfo(): String {
            return this.probe.name + if (this.parameters != null) "|" + this.parameters else ""
        }
    }

    override fun modifyAudioTrackPluginInfo(track: AudioTrack): JsonObject? {
        val (key, value) = when (track) {
            is LocalAudioTrack -> {
                "probeInfo" to track.containerTrackFactory.probeInfo()
            }

            is HttpAudioTrack -> {
                "probeInfo" to track.containerTrackFactory.probeInfo()
            }

            else -> return null
        }

        return JsonObject(mapOf(key to JsonPrimitive(value)))
    }
}
