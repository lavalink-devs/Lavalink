package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Suppress("DataClassPrivateConstructor")
@JsonClassDiscriminator("op")
@Serializable
sealed interface Message {
    val op: Op

    @Serializable
    enum class Op(val value: String) {
        @SerialName("ready")
        Ready("ready"),

        @SerialName("stats")
        Stats("stats"),

        @SerialName("playerUpdate")
        PlayerUpdate("playerUpdate"),

        @SerialName("event")
        Event("event");

        companion object {
            fun valueOfIgnoreCase(value: String): Op {
                return values().first { it.value.equals(value, true) }
            }
        }
    }

    @SerialName("ready")
    @Serializable
    data class ReadyEvent private constructor(
        override val op: Op,
        val resumed: Boolean,
        val sessionId: String,
    ) : Message {
        constructor(
            resumed: Boolean,
            sessionId: String
        ) : this(Op.Ready, resumed, sessionId)

    }

    @SerialName("playerEvent")
    @Serializable
    data class PlayerUpdateEvent private constructor(
        override val op: Op,
        val state: PlayerState,
        val guildId: String,
    ) : Message {
        constructor(
            state: PlayerState,
            guildId: String
        ) : this(Op.PlayerUpdate, state, guildId)

    }

    @SerialName("stats")
    @Serializable
    data class StatsEvent private constructor(
        override val op: Op,
        override val frameStats: FrameStats?,
        override val players: Int,
        override val playingPlayers: Int,
        override val uptime: Long,
        override val memory: Memory,
        override val cpu: Cpu,
    ) : Message, Stats {
        constructor(stats: Stats) : this(
            Op.Stats,
            stats.frameStats,
            stats.players,
            stats.playingPlayers,
            stats.uptime,
            stats.memory,
            stats.cpu
        )

    }

    @SerialName("event")
    @Serializable(with = EmittedEvent.Serializer::class)
    sealed interface EmittedEvent : Message {
        val type: Type
        val guildId: ULong

        @Serializable
        enum class Type(val value: String) {
            @SerialName("TrackStartEvent")
            TrackStart("TrackStartEvent"),

            @SerialName("TrackEndEvent")
            TrackEnd("TrackEndEvent"),

            @SerialName("TrackExceptionEvent")
            TrackException("TrackExceptionEvent"),

            @SerialName("TrackStuckEvent")
            TrackStuck("TrackStuckEvent"),

            @SerialName("WebSocketClosedEvent")
            WebSocketClosed("WebSocketClosedEvent");

            companion object {
                fun valueOfIgnoreCase(value: String): Type {
                    return values().first { it.value.equals(value, true) }
                }
            }
        }

        companion object Serializer : JsonContentPolymorphicSerializer<EmittedEvent>(EmittedEvent::class) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<EmittedEvent> {
                val typeRaw = element.jsonObject["type"] ?: error("Missing type")

                return when(Json.decodeFromJsonElement<Type>(typeRaw)) {
                    Type.TrackStart -> TrackStartEvent.serializer()
                    Type.TrackEnd -> TrackEndEvent.serializer()
                    Type.TrackException -> TrackExceptionEvent.serializer()
                    Type.TrackStuck -> TrackStuckEvent.serializer()
                    Type.WebSocketClosed -> WebSocketClosedEvent.serializer()
                }
            }
        }

        @Serializable
        data class TrackStartEvent private constructor(
            override val op: Op, override val type: Type, override val guildId: ULong,
            val track: Track
        ) : EmittedEvent {
            constructor(
                guildId: ULong,
                track: Track
            ) : this(Op.Event, Type.TrackStart, guildId, track)
        }

        @Serializable
        data class TrackEndEvent private constructor(
            override val op: Op, override val type: Type, override val guildId: ULong,
            val track: Track,
            val reason: AudioTrackEndReason,
        ) : EmittedEvent {
            constructor(
                guildId: ULong,
                track: Track,
                reason: AudioTrackEndReason
            ) : this(Op.Event, Type.TrackEnd, guildId, track, reason)

            /**
             * Reason why a track stopped playing.
             *
             * @property mayStartNext Indicates whether a new track should be started on receiving this event.
             * If this is false, either this event is already triggered because another track started (REPLACED)
             * or because the player is stopped (STOPPED, CLEANUP).
             */
            @Serializable
            enum class AudioTrackEndReason(val mayStartNext: Boolean) {
                /**
                 * This means that the track itself emitted a terminator. This is usually caused by the track reaching the end,
                 * however it will also be used when it ends due to an exception.
                 */
                FINISHED(true),

                /**
                 * This means that the track failed to start, throwing an exception before providing any audio.
                 */
                LOAD_FAILED(true),

                /**
                 * The track was stopped due to the player being stopped by either calling stop() or playTrack(null).
                 */
                STOPPED(false),

                /**
                 * The track stopped playing because a new track started playing. Note that with this reason, the old track will still
                 * play until either its buffer runs out or audio from the new track is available.
                 */
                REPLACED(false),

                /**
                 * The track was stopped because the cleanup threshold for the audio player was reached. This triggers when the amount
                 * of time passed since the last call to AudioPlayer#provide() has reached the threshold specified in player manager
                 * configuration. This may also indicate either a leaked audio player which was discarded, but not stopped.
                 */
                CLEANUP(false)

            }
        }

        @Serializable
        data class TrackExceptionEvent private constructor(
            override val op: Op, override val type: Type, override val guildId: ULong,
            val track: Track,
            val exception: Exception
        ) : EmittedEvent {
            constructor(
                guildId: ULong,
                track: Track,
                exception: Exception
            ) : this(Op.Event, Type.TrackException, guildId, track, exception)
        }

        @Serializable
        data class TrackStuckEvent private constructor(
            override val op: Op, override val type: Type, override val guildId: ULong,
            val track: Track,
            val thresholdMs: Long
        ) : EmittedEvent {
            constructor(
                guildId: ULong,
                track: Track,
                thresholdMs: Long
            ) : this(Op.Event, Type.TrackStuck, guildId, track, thresholdMs)

            val threshold by lazy { thresholdMs.toDuration(DurationUnit.MILLISECONDS) }
        }

        @Serializable
        data class WebSocketClosedEvent private constructor(
            override val op: Op, override val type: Type, override val guildId: ULong,
            val code: Int,
            val reason: String,
            val byRemote: Boolean
        ) : EmittedEvent {
            constructor(
                guildId: ULong,
                code: Int,
                reason: String,
                byRemote: Boolean
            ) : this(Op.Event, Type.WebSocketClosed, guildId, code, reason, byRemote)
        }
    }
}
