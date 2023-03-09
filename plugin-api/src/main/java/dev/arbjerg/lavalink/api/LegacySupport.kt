@file:JvmName("LegacySupport")

package dev.arbjerg.lavalink.api

import com.fasterxml.jackson.databind.JsonNode
import dev.arbjerg.lavalink.protocol.v3.objectMapper
import dev.arbjerg.lavalink.protocol.v4.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement

fun JsonElement.toJsonNode(): JsonNode = objectMapper().readTree(json.encodeToString(this))

fun JsonNode.toJsonElement(): JsonElement = json.decodeFromString(toString())
