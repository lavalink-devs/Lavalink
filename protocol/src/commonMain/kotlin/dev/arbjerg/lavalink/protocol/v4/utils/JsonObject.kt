@file:JvmName("JsonObjects")

package dev.arbjerg.lavalink.protocol.v4.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmName

/**
 * Returns an empty [JsonObject].
 */
@get:JvmName("empty")
val EMPTY_OBJECT = JsonObject(emptyMap())

/**
 * Returns a new [JavaJsonObjectBuilder].
 */
@JvmName("builder")
fun objectBuilder(): JavaJsonObjectBuilder = JavaJsonObjectBuilder(mutableMapOf())


/**
 * Json object builder.
 */
class JavaJsonObjectBuilder(private val contents: MutableMap<String, JsonElement>) :
    MutableMap<String, JsonElement> by contents {

    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: String) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: Number) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: Boolean) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to `null`.`
     */
    fun putNull(key: String) = apply { contents[key] = JsonNull }

    /**
     * Creates an array for [key].
     */
    fun putArray(key: String): ChildJavaJsonArrayBuilder = ChildJavaJsonArrayBuilder(key, this, mutableListOf())

    /**
     * Builds the object.
     */
    fun build() = JsonObject(contents)
}

class ChildJavaJsonObjectBuilder(
    private val arrayBuilder: JavaJsonArrayBuilder,
    private val contents: MutableMap<String, JsonElement>
) : MutableMap<String, JsonElement> by contents {

    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: String) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: Number) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to [value].
     */
    fun put(key: String, value: Boolean) = apply { contents[key] = JsonPrimitive(value) }
    /**
     * Puts [key] to `null`.
     */
    fun putNull(key: String) = apply { contents[key] = JsonNull }

    /**
     * Returns to the [JavaJsonObjectBuilder]
     */
    fun build() = arrayBuilder.apply {
        add(JsonObject(this@ChildJavaJsonObjectBuilder.contents))
    }
}
