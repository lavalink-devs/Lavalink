package dev.arbjerg.lavalink.protocol.v4

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

@JsonDeserialize(using = OmissibleDeserializer::class)
@JsonSerialize(using = OmissibleSerializer::class)
interface Omissible<T> {
    val isPresent: Boolean
    val value: T

    class Present<T>(override val value: T) : Omissible<T> {
        override val isPresent = true

        override fun toString() = value.toString()
    }

    object Omitted : Omissible<Nothing> {
        override val isPresent = false

        override val value: Nothing
            get() = error("Not present")

        override fun toString() = "OMITTED"
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> omitted() = Omitted as Omissible<T>
        fun <T> of(element: T) = Present(element)
    }
}

inline fun <T> Omissible<T>.takeIfPresent(function: (T) -> Unit) {
    if (isPresent) function(value)
}


class OmissibleDeserializer<T>(private val deserializer: (JsonParser, DeserializationContext) -> T) :
    StdDeserializer<Omissible<T>>(Omissible::class.java) {

    @Suppress("unused")
    constructor() : this({ jp, _ -> jp.readValueAs(object : TypeReference<T>() {}) })

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Omissible<T> {
        return if (jp.currentToken() == null) {
            Omissible.omitted()
        } else {
            Omissible.of(deserializer(jp, ctxt))
        }
    }

}

class OmissibleSerializer<T>(private val serializer: (T, JsonGenerator, SerializerProvider) -> Unit) :
    StdSerializer<Omissible<T>>(Omissible::class.java, false) {

    @Suppress("unused")
    constructor() : this({ value, gen, _ -> gen.writePOJO(value) })

    override fun isEmpty(provider: SerializerProvider?, value: Omissible<T>): Boolean {
        return value.isPresent.not()
    }

    override fun serialize(value: Omissible<T>, gen: JsonGenerator, provider: SerializerProvider) {
        if (!value.isPresent) {
            gen.writeNull()
        } else {
            serializer(value.value, gen, provider)
        }
    }
}

