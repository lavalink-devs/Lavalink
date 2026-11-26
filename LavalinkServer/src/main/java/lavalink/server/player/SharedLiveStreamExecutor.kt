package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerOptions
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerTracker
import com.sedmelluq.discord.lavaplayer.track.TrackStateListener
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBuffer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameProviderTools
import com.sedmelluq.discord.lavaplayer.track.playback.AudioTrackExecutor
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import com.sedmelluq.discord.lavaplayer.track.playback.TerminatorAudioFrame
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

internal class SharedLiveStreamExecutor(
    private val track: HttpAudioTrack,
    private val player: DefaultAudioPlayer,
    private val options: AudioPlayerOptions,
    private val configuration: AudioConfiguration,
    private val streams: SharedLiveStreams,
    private val playback: Executor,
    private val bufferDuration: Int
) : AudioTrackExecutor, TrackStateListener {
    private val lock = Any()
    private val markers = TrackMarkerTracker()
    private var subscription: SharedLiveStreams.Subscription? = null
    @Volatile private var dedicated: LocalAudioTrackExecutor? = null
    @Volatile private var position = 0L
    @Volatile private var stopped = false
    @Volatile private var ended = false
    @Volatile private var failure: FriendlyException? = null

    override fun getAudioBuffer(): AudioFrameBuffer? = dedicated?.audioBuffer

    // Shared audio starts on the first frame request from a guild that is playing.
    override fun execute(listener: TrackStateListener) = Unit

    override fun stop() {
        synchronized(lock) {
            stopped = true
            detach()
            dedicated?.stop()
        }
        markers.trigger(if (ended) MarkerState.ENDED else MarkerState.STOPPED)
    }

    fun pauseChanged() = synchronized(lock) {
        if (player.isPaused) detach()
    }

    override fun getPosition() = position
    override fun setPosition(timecode: Long) = Unit
    override fun getState() = when {
        stopped || ended -> AudioTrackState.FINISHED
        position > 0 -> AudioTrackState.PLAYING
        else -> AudioTrackState.LOADING
    }

    override fun setMarker(marker: TrackMarker?) = markers.set(marker, position)
    override fun addMarker(marker: TrackMarker?) = markers.add(marker, position)
    override fun removeMarker(marker: TrackMarker?) = markers.remove(marker)
    override fun failedBeforeLoad() = failure != null && position == 0L

    private fun detach() {
        subscription?.let(streams::release)
        subscription = null
    }

    private fun subscriptionForPlayback(): SharedLiveStreams.Subscription? = synchronized(lock) {
        if (stopped || player.isPaused || player.playingTrack !== track || dedicated != null) return null
        if (options.filterFactory.get() != null) {
            val copy = track.makeClone() as InternalAudioTrack
            val executor = LocalAudioTrackExecutor(copy, configuration, options, false, bufferDuration)
            copy.assignExecutor(executor, false)
            detach()
            dedicated = executor
            playback.execute { executor.execute(this) }
            return null
        }

        val volume = options.volumeLevel.get()
        val current = subscription
        if (current?.volume == volume) return current

        // Subscribe first so a volume change in the only listening guild keeps the station running.
        val next = streams.subscribe(track, configuration, bufferDuration, volume)
        detach()
        subscription = next
        next
    }

    override fun provide(): AudioFrame? = AudioFrameProviderTools.delegateToTimedProvide(this)

    override fun provide(timeout: Long, unit: TimeUnit): AudioFrame? {
        if (ended) return TerminatorAudioFrame.INSTANCE
        if (stopped || player.isPaused) return null
        val active = subscriptionForPlayback()
        val frame = if (active != null) active.nextFrame(timeout, unit) else dedicated?.provide(timeout, unit)
        if (frame == null || stopped) return null

        if (frame.isTerminator) {
            val error = synchronized(lock) {
                if (stopped || (active != null && active !== subscription)) return null
                if (ended) return frame
                ended = true
                failure = active?.failure ?: failure
                failure
            }
            if (active != null && error != null) player.onTrackException(track, error)
        } else {
            val timecode = synchronized(lock) {
                if (stopped || (active != null && active !== subscription)) return null
                position += DISCORD_OPUS.frameDuration()
                position
            }
            markers.checkPlaybackTimecode(timecode)
        }
        return frame
    }

    override fun provide(targetFrame: MutableAudioFrame): Boolean = provide(targetFrame, 0, TimeUnit.MILLISECONDS)

    override fun provide(targetFrame: MutableAudioFrame, timeout: Long, unit: TimeUnit): Boolean {
        val frame = provide(timeout, unit) ?: return false
        targetFrame.isTerminator = frame.isTerminator
        if (!frame.isTerminator) {
            val bytes = frame.data
            targetFrame.store(bytes, 0, bytes.size)
            targetFrame.format = frame.format
            targetFrame.volume = frame.volume
            targetFrame.timecode = position
        }
        return true
    }

    override fun onTrackException(track: AudioTrack, exception: FriendlyException) {
        failure = exception
        if (!stopped) player.onTrackException(this.track, exception)
    }

    override fun onTrackStuck(track: AudioTrack, thresholdMs: Long) = Unit
}
