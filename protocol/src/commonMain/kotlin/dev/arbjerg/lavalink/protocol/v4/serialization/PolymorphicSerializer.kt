package dev.arbjerg.lavalink.protocol.v4.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

internal inline fun <reified Type : Any, T> JsonElement.asPolymorphicDeserializer(
    descriptor: SerialDescriptor,
    typeName: String,
    crossinline deserialize: (Type) -> DeserializationStrategy<T>
): DeserializationStrategy<T> {
    val typeRaw = jsonObject[typeName] ?: error("Could not find $typeName")
    return object : KSerializer<T> {
        override val descriptor: SerialDescriptor
            get() = descriptor

        override fun deserialize(decoder: Decoder): T {
            val jsonDecoder = (decoder as? JsonDecoder) ?: error("Deserializer only supports json, but got: $decoder")
            return deserialize(jsonDecoder.json.decodeFromJsonElement(typeRaw))
                .deserialize(decoder)
        }

        override fun serialize(encoder: Encoder, value: T) {
            throw UnsupportedOperationException("this is only a deserializer")
        }
    }
}
