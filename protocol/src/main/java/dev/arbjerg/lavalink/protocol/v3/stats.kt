package dev.arbjerg.lavalink.protocol.v3

data class Stats(
    val frameStats: FrameStats?,
    val players: Int,
    val playingPlayers: Int,
    val uptime: Long,
    val memory: Memory,
    val cpu: Cpu,
)

data class FrameStats(
    val sent: Long,
    val nulled: Long,
    val deficit: Long
)

data class Memory(
    val free: Long,
    val used: Long,
    val allocated: Long,
    val reservable: Long
)

data class Cpu(
    val cores: Int,
    val systemLoad: Double,
    val lavalinkLoad: Double
)