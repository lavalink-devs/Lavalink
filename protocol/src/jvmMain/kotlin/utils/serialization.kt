@file:JvmName("Serialization")
package dev.arbjerg.lavalink.protocol.v4.utils

import dev.arbjerg.lavalink.protocol.v4.LavalinkSerializersModule
import dev.arbjerg.lavalink.protocol.v4.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.jvm.JvmName

/**
 * Serializes a [JsonElement] into a String.
 */
fun serialize(element: JsonElement) = json.encodeToString(element)

/**
 * Deserializes [source] into an [JsonElement].
 */
fun deserializeJsonElement(source: String): JsonElement = json.decodeFromString(source)
/**
 * Deserializes [source] into an [JsonObject].
 */
fun deserializeJsonObject(source: String): JsonObject = json.decodeFromString(source)
/**
 * Deserializes [source] into an [JsonArray].
 */
fun deserializeJsonArray(source: String): JsonArray = json.decodeFromString(source)
