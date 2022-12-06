package dev.arbjerg.lavalink.protocol.v3

data class Plugins(val plugins: List<Plugin>)

data class Plugin(val name: String, val version: String)