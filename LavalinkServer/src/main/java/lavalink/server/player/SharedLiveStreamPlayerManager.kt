package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerOptions
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackStateListener

internal class SharedLiveStreamPlayerManager : DefaultAudioPlayerManager() {
    private val streams = SharedLiveStreams(executor)

    override fun constructPlayer(): DefaultAudioPlayer = object : DefaultAudioPlayer(this) {
        override fun setPaused(value: Boolean) {
            super.setPaused(value)
            ((playingTrack as? InternalAudioTrack)?.activeExecutor as? SharedLiveStreamExecutor)?.pauseChanged()
        }
    }

    override fun executeTrack(
        listener: TrackStateListener,
        track: InternalAudioTrack,
        configuration: AudioConfiguration,
        playerOptions: AudioPlayerOptions
    ) {
        if (track is HttpAudioTrack && track.javaClass == HttpAudioTrack::class.java &&
            track.info.isStream && !track.isSeekable &&
            configuration.outputFormat == DISCORD_OPUS && playerOptions.filterFactory.get() == null &&
            listener is DefaultAudioPlayer
        ) {
            val playback = SharedLiveStreamExecutor(
                track, listener, playerOptions, configuration, streams, executor,
                playerOptions.frameBufferDuration.get() ?: frameBufferDuration
            )
            track.assignExecutor(playback, true)
        } else {
            super.executeTrack(listener, track, configuration, playerOptions)
        }
    }

    override fun shutdown() {
        streams.close()
        super.shutdown()
    }
}
