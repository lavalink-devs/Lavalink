package dev.arbjerg.lavalink.protocol

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

class MessageDeserializer : StdDeserializer<Message>(Message::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message {
        val node: JsonNode = jp.codec.readTree(jp)
        if (!node.has("op")) {
            throw IllegalArgumentException("Message is missing op field")
        }

        return when (Message.Op.valueOfIgnoreCase(node.get("op").asText())) {
            Message.Op.Ready -> jp.codec.treeToValue(node, Message.Ready::class.java)
            Message.Op.Stats -> jp.codec.treeToValue(node, Message.Stats::class.java)
            Message.Op.PlayerUpdate -> jp.codec.treeToValue(node, Message.PlayerUpdate::class.java)
            Message.Op.Event -> jp.codec.treeToValue(node, Message.Event::class.java)
        }
    }
}

sealed class Message(var op: Op) {

    enum class Op(@JsonValue val value: String) {
        Ready("ready"),
        Stats("stats"),
        PlayerUpdate("playerUpdate"),
        Event("event");

        companion object {
            fun valueOfIgnoreCase(value: String): Op {
                return values().first { it.value.equals(value, true) }
            }
        }
    }

    enum class EventType(@JsonValue val value: String) {
        TrackStart("TrackStartEvent"),
        TrackEnd("TrackEndEvent"),
        TrackException("TrackExceptionEvent"),
        TrackStuck("TrackStuckEvent"),
        WebSocketClosed("WebSocketClosedEvent");

        companion object {
            fun valueOfIgnoreCase(value: String): EventType {
                return values().first { it.value.equals(value, true) }
            }
        }
    }

    class EventDeserializer : StdDeserializer<Event>(Event::class.java) {
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Event {
            val node: JsonNode = jp.codec.readTree(jp)
            if (!node.has("type")) {
                throw IllegalArgumentException("Message is missing type field")
            }

            return when (EventType.valueOfIgnoreCase(node.get("type").asText())) {
                EventType.TrackStart -> jp.codec.treeToValue(node, TrackStartEvent::class.java)
                EventType.TrackEnd -> jp.codec.treeToValue(node, TrackEndEvent::class.java)
                EventType.TrackException -> jp.codec.treeToValue(node, TrackExceptionEvent::class.java)
                EventType.TrackStuck -> jp.codec.treeToValue(node, TrackStuckEvent::class.java)
                EventType.WebSocketClosed -> jp.codec.treeToValue(node, WebSocketClosedEvent::class.java)
            }
        }
    }

    sealed class Event(
        val type: EventType,
        open val guildId: String
    ) : Message(Op.Event)

    data class Ready(
        val resumed: Boolean,
        val sessionId: String,
    ) : Message(Op.Ready)

    data class PlayerUpdate(
        val state: PlayerState,
        val guildId: String,
    ) : Message(Op.PlayerUpdate)

    data class Stats(
        val frameStats: FrameStats?,
        val players: Int,
        val playingPlayers: Int,
        val uptime: Long,
        val memory: Memory,
        val cpu: Cpu,
    ) : Message(Op.Stats)

    data class FrameStats(
        val sent: Long,
        val nulled: Long,
        val deficit: Long
    )

    data class Memory(
        val free: Long,
        val used: Long,
        val allocated: Long,
        val reservable: Long
    )

    data class Cpu(
        val cores: Int,
        val systemLoad: Double,
        val lavalinkLoad: Double
    )

    data class TrackStartEvent(
        val encodedTrack: String,
        val track: String,
        override val guildId: String,
    ) : Event(EventType.TrackStart, guildId)

    data class TrackEndEvent(
        val encodedTrack: String,
        val track: String,
        val reason: AudioTrackEndReason,
        override val guildId: String,
    ) : Event(EventType.TrackEnd, guildId)

    data class TrackExceptionEvent(
        val encodedTrack: String,
        val track: String,
        val exception: Exception,
        override val guildId: String,
    ) : Event(EventType.TrackException, guildId)

    data class TrackStuckEvent(
        val encodedTrack: String,
        val track: String,
        val thresholdMs: Long,
        override val guildId: String,
    ) : Event(EventType.TrackStuck, guildId)

    data class WebSocketClosedEvent(
        val code: Int,
        val reason: String,
        val byRemote: Boolean,
        override val guildId: String,
    ) : Event(EventType.WebSocketClosed, guildId)
}