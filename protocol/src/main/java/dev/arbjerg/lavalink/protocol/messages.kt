package dev.arbjerg.lavalink.protocol

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

class MessageDeserializer : StdDeserializer<Message>(Message::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message {
        val node: JsonNode = jp.codec.readTree(jp)
        if (!node.has("op")) {
            throw IllegalArgumentException("Message is missing op field")
        }

        return when (Message.Op.valueOfIgnoreCase(node.get("op").asText())) {
            Message.Op.Ready -> jp.codec.treeToValue(node, Message.ReadyEvent::class.java)
            Message.Op.Stats -> jp.codec.treeToValue(node, Message.StatsEvent::class.java)
            Message.Op.PlayerUpdate -> jp.codec.treeToValue(node, Message.PlayerUpdateEvent::class.java)
            Message.Op.Event -> jp.codec.treeToValue(node, Message.Event::class.java)
        }
    }
}

class EventDeserializer : StdDeserializer<Message.Event>(Message.Event::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Message.Event {
        val node: JsonNode = jp.codec.readTree(jp)
        if (!node.has("type")) {
            throw IllegalArgumentException("Message is missing type field")
        }

        return when (Message.EventType.valueOfIgnoreCase(node.get("type").asText())) {
            Message.EventType.TrackStart -> jp.codec.treeToValue(node, Message.TrackStartEvent::class.java)
            Message.EventType.TrackEnd -> jp.codec.treeToValue(node, Message.TrackEndEvent::class.java)
            Message.EventType.TrackException -> jp.codec.treeToValue(node, Message.TrackExceptionEvent::class.java)
            Message.EventType.TrackStuck -> jp.codec.treeToValue(node, Message.TrackStuckEvent::class.java)
            Message.EventType.WebSocketClosed -> jp.codec.treeToValue(node, Message.WebSocketClosedEvent::class.java)
        }
    }
}

@JsonDeserialize(using = MessageDeserializer::class)
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

    @JsonDeserialize(using = EventDeserializer::class)
    sealed class Event(
        val type: EventType,
        open val guildId: String
    ) : Message(Op.Event)

    data class ReadyEvent(
        val resumed: Boolean,
        val sessionId: String,
    ) : Message(Op.Ready)

    data class PlayerUpdateEvent(
        val state: PlayerState,
        val guildId: String,
    ) : Message(Op.PlayerUpdate)

    data class StatsEvent(
        @JsonUnwrapped
        val stats: Stats
    ) : Message(Op.Stats)

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