@file:JvmName("JsonPrimitives")

package dev.arbjerg.lavalink.protocol.v4.utils

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.jvm.JvmName

/**
 * Returns a Json representation of `null`.
 */
fun jsonNull() = JsonNull

/**
 * Returns the [JsonPrimitive] representing [string].
 */
fun fromString(string: String) = JsonPrimitive(string)

/**
 * Returns the [JsonPrimitive] representing [number].
 */
fun fromNumber(number: Number) = JsonPrimitive(number)
