package lavalink.server.io

import com.fasterxml.jackson.databind.node.ObjectNode
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.arbjerg.lavalink.api.AudioPluginInfoModifier
import org.springframework.stereotype.Component

@Component
class DefaultAudioPluginInfoModifier : AudioPluginInfoModifier {

    companion object {
        fun MediaContainerDescriptor.probeInfo(): String {
            return this.probe.name + if (this.parameters != null) "|" + this.parameters else ""
        }
    }

    override fun modifyAudioTrackPluginInfo(track: AudioTrack, node: ObjectNode) {
        when (track) {
            is LocalAudioTrack -> {
                node.put("probeInfo", track.containerTrackFactory.probeInfo())
            }

            is HttpAudioTrack -> {
                node.put("probeInfo", track.containerTrackFactory.probeInfo())
            }
        }
    }
}