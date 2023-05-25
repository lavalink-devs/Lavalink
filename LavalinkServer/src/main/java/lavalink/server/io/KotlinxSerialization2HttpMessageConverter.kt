package lavalink.server.io

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractHttpMessageConverter
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
class KotlinxSerialization2HttpMessageConverter(private val json: Json = Json) : AbstractHttpMessageConverter<Any>() {
    override fun supports(clazz: Class<*>): Boolean = clazz.kotlin.serializerOrNull() != null
    override fun getSupportedMediaTypes(): MutableList<MediaType> = mutableListOf(MediaType.APPLICATION_JSON)

    override fun writeInternal(t: Any, outputMessage: HttpOutputMessage) {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> T.serialize() {
            val serializer = (this::class as KClass<T>).serializer()
            json.encodeToStream(serializer, this, outputMessage.body)
        }
        t.serialize()
    }

    override fun readInternal(clazz: Class<out Any>, inputMessage: HttpInputMessage): Any {
        fun <T : Any> Class<T>.deserialize(): T {
            val serializer = kotlin.serializer()
            return json.decodeFromStream(serializer, inputMessage.body)
        }

        return clazz.deserialize()
    }
}
