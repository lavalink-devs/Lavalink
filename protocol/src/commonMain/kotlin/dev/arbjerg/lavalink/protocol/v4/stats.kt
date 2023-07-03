package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.Serializable

/**
 * Representation of node stats.
 *
 * @property frameStats the frame stats of the node. null if the node has no players or when retrieved via `/v4/stats`
 * @property players the amount of players connected to the node
 * @property playingPlayers the amount of players playing a track
 * @property uptime the uptime of the node in milliseconds
 * @property memory the memory stats of the node
 * @property cpu the cpu stats of the node
 */
interface Stats {
    val frameStats: FrameStats?
    val players: Int
    val playingPlayers: Int
    val uptime: Long
    val memory: Memory
    val cpu: Cpu
}

/**
 * Default implementation of [Stats].
 */
@Serializable
data class StatsData(
    override val frameStats: FrameStats? = null,
    override val players: Int,
    override val playingPlayers: Int,
    override val uptime: Long,
    override val memory: Memory,
    override val cpu: Cpu
) : Stats

/**
 * Frame statistics.
 *
 * @property sent the amount of frames sent to Discord
 * @property nulled the amount of frames that were nulled
 * @property deficit the amount of frames that were deficit
 */
@Serializable
data class FrameStats(
    val sent: Int,
    val nulled: Int,
    val deficit: Int
)

/**
 * Memory statistics.
 *
 * @property free the amount of free memory in bytes
 * @property used the amount of used memory in bytes
 * @property allocated the amount of allocated memory in bytes
 * @property reservable the amount of reservable memory in bytes
 */
@Serializable
data class Memory(
    val free: Long,
    val used: Long,
    val allocated: Long,
    val reservable: Long
)

/**
 * Cpu statistics.
 *
 * @property cores the amount of cores the node has
 * @property systemLoad the system load of the node
 * @property lavalinkLoad the load of Lavalink on the node
 */
@Serializable
data class Cpu(
    val cores: Int,
    val systemLoad: Double,
    val lavalinkLoad: Double
)
