package dev.arbjerg.lavalink.protocol

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize


data class Session(
    val resumingKey: String? = null,
    val timeout: Long,
)

@JsonDeserialize(using = SessionUpdateDeserializer::class)
data class SessionUpdate(
    val resumingKey: Omissible<String?> = Omissible.omitted(),
    val timeout: Omissible<Long> = Omissible.omitted(),
)

class SessionUpdateDeserializer : JsonDeserializer<SessionUpdate>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SessionUpdate {
        val node = p.codec.readTree<JsonNode>(p)

        val resumingKey = node.get("resumingKey")?.let {
            if (it.isNull) Omissible.of<String?>(null) else Omissible.of(it.asText())
        } ?: Omissible.omitted()

        val timeout = node.get("timeout")?.let {
            Omissible.of(it.asLong())
        } ?: Omissible.omitted()

        return SessionUpdate(
            resumingKey,
            timeout
        )
    }

}