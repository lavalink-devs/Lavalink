package dev.arbjerg.lavalink.protocol.v4.serialization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Representation of a Unix timestamp in milliseconds.
 *
 * @see Instant
 * @see TimestampSerializer
 */
typealias Timestamp = @Serializable(with = TimestampSerializer::class) Instant

/**
 * Serializer of [Instant] using [Instant.fromEpochMilliseconds] and [Instant.toEpochMilliseconds].
 */
object TimestampSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.LONG)
    override fun deserialize(decoder: Decoder): Instant = Instant.fromEpochMilliseconds(decoder.decodeLong())
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeLong(value.toEpochMilliseconds())
}
