package dev.arbjerg.lavalink.protocol.v3

import com.fasterxml.jackson.annotation.JsonValue

data class Plugins(
    @JsonValue
    val plugins: List<Plugin>
)

data class Plugin(val name: String, val version: String)