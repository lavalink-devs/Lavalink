package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

@Serializable(with = OmissableSerializer::class)
sealed interface Omissible<out T> {
    @JvmInline
    value class Present<out T>(val value: T) : Omissible<T> {
        override fun toString() = value.toString()
    }

    class Omitted<out T> private constructor() : Omissible<T> {
        override fun toString() = "OMITTED"

        companion object {
            private val constantOmitted = Omitted<Nothing>()
            operator fun <T : Any> invoke(): Omitted<T> = constantOmitted
        }
    }

    companion object {
        fun <T> of(element: T) = Present(element)
    }
}

inline fun <T> Omissible<T>.takeIfPresent(function: (T) -> Unit) {
    if (this is Omissible.Present) function(value)
}

inline fun <T, R> Omissible<T>.map(mapper: (T) -> R) = when (this) {
    is Omissible.Omitted -> Omissible.Omitted()
    is Omissible.Present -> Omissible.of(mapper(value))
}

class OmissableSerializer<T>(private val childSerializer: KSerializer<T>) : KSerializer<Omissible<T>> {
    override val descriptor: SerialDescriptor = childSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Omissible<T>) {
        when (value) {
            is Omissible.Omitted -> throw SerializationException("Omissable.Omitted cannot be serialized, try using encodeDefaults = false")
            is Omissible.Present -> encoder.encodeInline(descriptor)
                .encodeSerializableValue(childSerializer, value.value)
        }
    }

    override fun deserialize(decoder: Decoder): Omissible<T> {
        /**
         * let's clear up any inconsistencies, an Optional cannot be <T: Any> and be represented as nullable.
         */
        if (!descriptor.isNullable && !decoder.decodeNotNullMark()) {
            throw SerializationException("descriptor for ${descriptor.serialName} was not nullable but null mark was encountered")
        }

        val child = decoder.decodeInline(descriptor).decodeSerializableValue(childSerializer)

        return Omissible.of(child)
    }
}
