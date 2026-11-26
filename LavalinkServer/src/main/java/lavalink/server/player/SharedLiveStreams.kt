package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.container.MediaContainerProbe
import com.sedmelluq.discord.lavaplayer.filter.volume.PcmVolumeProcessor
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_OPUS
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.DISCORD_PCM_S16_LE
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerOptions
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack
import com.sedmelluq.discord.lavaplayer.track.TrackStateListener
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import com.sedmelluq.discord.lavaplayer.track.playback.ImmutableAudioFrame
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor
import com.sedmelluq.discord.lavaplayer.track.playback.TerminatorAudioFrame
import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executor
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class SharedLiveStreams(private val playback: Executor) : AutoCloseable {
    private val clock = ScheduledThreadPoolExecutor(1, DaemonThreadFactory("shared-live-stream")).apply {
        removeOnCancelPolicy = true
    }
    private val stations = mutableMapOf<StationKey, Station>()

    @Synchronized
    fun subscribe(track: HttpAudioTrack, configuration: AudioConfiguration, bufferDuration: Int, volume: Int): Subscription {
        val container = track.containerTrackFactory
        val key = StationKey(track.identifier, track.sourceManager, container.probe, container.parameters)
        val station = stations[key]
        station?.subscribe(volume)?.let { return it }

        check(!clock.isShutdown) { "Shared live streams have shut down" }
        val replacement = Station(key, track, configuration, bufferDuration)
        val subscription = checkNotNull(replacement.subscribe(volume))
        stations[key] = replacement
        try {
            replacement.start()
        } catch (e: RuntimeException) {
            release(subscription)
            throw e
        }
        return subscription
    }

    @Synchronized
    fun release(subscription: Subscription) {
        val station = subscription.station
        if (station.unsubscribe(subscription)) {
            stations.remove(station.key, station)
            station.close()
        }
    }

    @Synchronized
    override fun close() {
        stations.values.forEach { it.close() }
        stations.clear()
        clock.shutdown()
    }

    internal data class StationKey(
        val url: String,
        val source: AudioSourceManager,
        val probe: MediaContainerProbe,
        val parameters: String?
    )

    internal class Subscription(val station: Station, val volume: Int, var cursor: Long) {
        var closed = false
        var started = false

        fun nextFrame(timeout: Long, unit: TimeUnit): AudioFrame? = station.nextFrame(this, timeout, unit)
        val failure: FriendlyException? get() = station.failure
    }

    internal inner class Station(
        val key: StationKey,
        track: HttpAudioTrack,
        private val configuration: AudioConfiguration,
        private val bufferDuration: Int
    ) {
        private val lock = Object()
        private val volumes = mutableMapOf<Int, VolumeStream>()
        private val pcm = ShortArray(DISCORD_PCM_S16_LE.totalSampleCount())
        private val sourceTemplate = (track.makeClone() as HttpAudioTrack).apply { userData = null }
        private val sourceConfiguration = configuration.copy().apply { outputFormat = DISCORD_PCM_S16_LE }
        private var upstream: Upstream? = null
        private var clockTask: ScheduledFuture<*>? = null
        private var sequence = 0L
        private var ended = false
        private var closed = false
        private var upstreamStartSequence = 0L
        private var retryCount = 0
        private var retryAtNanos = 0L
        @Volatile var failure: FriendlyException? = null
            private set

        fun start() = synchronized(lock) {
            openUpstream()
            val frameDuration = DISCORD_OPUS.frameDuration()
            clockTask = clock.scheduleAtFixedRate(::publish, 0, frameDuration, TimeUnit.MILLISECONDS)
        }

        private fun openUpstream() {
            val input = Upstream(sourceTemplate, sourceConfiguration, minOf(bufferDuration, MAX_PCM_BUFFER_MS))
            upstream = input
            upstreamStartSequence = sequence
            playback.execute(input::run)
        }

        fun subscribe(volume: Int): Subscription? = synchronized(lock) {
            if (ended) return null
            volumes.getOrPut(volume) { VolumeStream(volume, configuration) }.listeners++
            Subscription(this, volume, sequence)
        }

        fun unsubscribe(subscription: Subscription): Boolean = synchronized(lock) {
            if (!subscription.closed) {
                subscription.closed = true
                val stream = volumes.getValue(subscription.volume)
                stream.listeners--
                if (stream.listeners == 0) {
                    volumes.remove(subscription.volume)
                    stream.close()
                }
                lock.notifyAll()
            }
            volumes.isEmpty()
        }

        fun nextFrame(subscription: Subscription, timeout: Long, unit: TimeUnit): AudioFrame? = synchronized(lock) {
            val deadline = System.nanoTime() + unit.toNanos(timeout)
            while (!subscription.closed) {
                if (closed) return TerminatorAudioFrame.INSTANCE
                val available = sequence - subscription.cursor
                if (available > 0 && (subscription.started || available >= STARTUP_FRAMES || ended)) {
                    if (available > FRAME_HISTORY) subscription.cursor = sequence - STARTUP_FRAMES
                    val frame = volumes.getValue(subscription.volume).frames[(subscription.cursor % FRAME_HISTORY).toInt()]
                    subscription.cursor++
                    subscription.started = true
                    return frame
                }
                if (ended) return TerminatorAudioFrame.INSTANCE
                if (timeout <= 0) return null
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0) throw TimeoutException()
                TimeUnit.NANOSECONDS.timedWait(lock, remaining)
            }
            null
        }

        private fun publish(): Unit = synchronized(lock) {
            if (ended) return
            try {
                val input = upstream
                if (input == null) {
                    if (System.nanoTime() - retryAtNanos >= 0) openUpstream()
                    return
                }

                val frame = input.source.provide()
                if (frame == null || frame.isTerminator) {
                    if (input.finished) onUpstreamFinished(input.failure)
                    return
                }
                ByteBuffer.wrap(frame.data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(pcm)
                val index = (sequence % FRAME_HISTORY).toInt()
                for (stream in volumes.values) {
                    stream.frames[index] = stream.encode(pcm, frame.timecode)
                }
                sequence++
                lock.notifyAll()
            } catch (e: RuntimeException) {
                log.error("Failed to distribute shared live stream audio", e)
                failure = FriendlyException("Failed to distribute shared live stream audio.", FriendlyException.Severity.FAULT, e)
                finish()
                upstream?.close()
            }
        }

        private fun onUpstreamFinished(error: FriendlyException?) {
            upstream = null
            if (sequence - upstreamStartSequence >= HEALTHY_PLAYBACK_FRAMES) retryCount = 0

            if (error == null || retryCount == RETRY_DELAYS_MS.size) {
                failure = error
                finish()
                return
            }

            // Finish draining the old decoder before opening a fresh connection at the live point.
            retryAtNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(RETRY_DELAYS_MS[retryCount++])
            log.info("Reconnecting shared live stream after a playback error (attempt {}/{})", retryCount, RETRY_DELAYS_MS.size)
        }

        private fun finish() {
            ended = true
            clockTask?.cancel(false)
            lock.notifyAll()
        }

        fun close() = synchronized(lock) {
            if (!closed) {
                closed = true
                finish()
                volumes.values.forEach { it.close() }
                upstream?.close()
            }
        }
    }

    private class Upstream(track: HttpAudioTrack, configuration: AudioConfiguration, bufferDuration: Int) : TrackStateListener {
        private val sourceTrack = track.makeClone() as InternalAudioTrack
        val source = LocalAudioTrackExecutor(sourceTrack, configuration, AudioPlayerOptions(), false, bufferDuration)
        @Volatile var finished = false
            private set
        @Volatile var failure: FriendlyException? = null
            private set

        init {
            sourceTrack.assignExecutor(source, false)
        }

        fun run() {
            try {
                source.execute(this)
            } finally {
                finished = true
            }
        }

        fun close() {
            if (!finished) sourceTrack.stop()
        }

        override fun onTrackException(track: AudioTrack, exception: FriendlyException) {
            failure = exception
        }

        override fun onTrackStuck(track: AudioTrack, thresholdMs: Long) = Unit
    }

    private class VolumeStream(private val volume: Int, configuration: AudioConfiguration) : AutoCloseable {
        val frames = arrayOfNulls<AudioFrame>(FRAME_HISTORY)
        var listeners = 0
        private val encoder = lazy { DISCORD_OPUS.createEncoder(configuration) }
        private val volumeProcessor = PcmVolumeProcessor(100)
        private var closed = false
        private val samples = lazy {
            ByteBuffer.allocateDirect(DISCORD_PCM_S16_LE.maximumChunkSize()).order(ByteOrder.nativeOrder()).asShortBuffer()
        }

        fun encode(pcm: ShortArray, timecode: Long): AudioFrame {
            val bytes = if (volume == 0) {
                DISCORD_OPUS.silenceBytes()
            } else {
                val buffer = samples.value
                buffer.clear().put(pcm).flip()
                volumeProcessor.applyVolume(100, volume, buffer)
                encoder.value.encode(buffer)
            }
            return ImmutableAudioFrame(timecode, bytes, volume, DISCORD_OPUS)
        }

        override fun close() {
            if (!closed) {
                closed = true
                if (encoder.isInitialized()) encoder.value.close()
            }
        }
    }

    private companion object {
        // PCM frames are larger than encoded Opus frames, so cap the upstream buffer.
        const val MAX_PCM_BUFFER_MS = 1_000
        const val FRAME_HISTORY = 10
        const val STARTUP_FRAMES = 2
        val HEALTHY_PLAYBACK_FRAMES = TimeUnit.SECONDS.toMillis(5) / DISCORD_OPUS.frameDuration()
        val RETRY_DELAYS_MS = longArrayOf(250, 1_000)
        val log = LoggerFactory.getLogger(SharedLiveStreams::class.java)
    }
}
