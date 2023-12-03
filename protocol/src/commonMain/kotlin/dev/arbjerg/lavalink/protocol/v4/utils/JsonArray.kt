@file:JvmName("JsonArrays")

package dev.arbjerg.lavalink.protocol.v4.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmName

/**
 * Returns an empty [JsonArray].
 */
@get:JvmName("empty")
val EMPTY_ARRAY = JsonArray(emptyList())

/**
 * Creates a new [JavaJsonArrayBuilder].
 */
@JvmName("builder")
fun arrayBuilder(): JavaJsonArrayBuilder = JavaJsonArrayBuilder(mutableListOf())


/**
 * Json Array builder
 */
class JavaJsonArrayBuilder internal constructor(private val contents: MutableList<JsonElement>) :
    MutableList<JsonElement> by contents {

    /**
     * Adds [value].
     */
    fun add(value: String) = apply { contents.add(JsonPrimitive(value)) }
    /**
     * Adds [value].
     */
    fun add(value: Number) = apply { contents.add(JsonPrimitive(value)) }
    /**
     * Adds `null`.
     */
    fun addNull() = apply { contents.add(JsonPrimitive(null)) }

    /**
     * Builds and adds a new JsonObject.
     */
    fun addObject() = ChildJavaJsonObjectBuilder(this, mutableMapOf())

    fun build() = JsonArray(contents)
}

class ChildJavaJsonArrayBuilder(
    private val key: String,
    private val objectBuilder: JavaJsonObjectBuilder,
    private val contents: MutableList<JsonElement>
) : MutableList<JsonElement> by contents {
    /**
     * Adds [value].
     */
    fun add(value: String) = apply { contents.add(JsonPrimitive(value)) }
    /**
     * Adds [value].
     */
    fun add(value: Number) = apply { contents.add(JsonPrimitive(value)) }
    /**
     * Adds `null`.
     */
    fun addNull() = apply { contents.add(JsonPrimitive(null)) }
    fun build() = objectBuilder.apply {
        put(key, JsonArray(this@ChildJavaJsonArrayBuilder.contents))
    }
}
