package dev.arbjerg.lavalink.protocol.v4.serialization

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Representation of a serializable [HttpStatusCode].
 *
 * @see HttpStatusCode
 * @see HttpStatusSerializer
 */
typealias SerializableHttpStatus = @Serializable(with = HttpStatusSerializer::class) HttpStatusCode

/**
 * Serializer of [HttpStatusCode] using [HttpStatusCode.value] and [HttpStatusCode.fromValue].
 */
object HttpStatusSerializer : KSerializer<HttpStatusCode> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HttpStatus", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): HttpStatusCode = HttpStatusCode.fromValue(decoder.decodeInt())
    override fun serialize(encoder: Encoder, value: HttpStatusCode) = encoder.encodeInt(value.value)
}
