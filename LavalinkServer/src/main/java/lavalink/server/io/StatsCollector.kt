/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package lavalink.server.io

import dev.arbjerg.lavalink.protocol.v4.*
import lavalink.server.Launcher
import lavalink.server.player.AudioLossCounter
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import oshi.SystemInfo
import kotlin.Exception

@RestController
class StatsCollector(val socketServer: SocketServer) {
    companion object {
        private val log = LoggerFactory.getLogger(StatsCollector::class.java)

        private val si = SystemInfo()
        private val hal get() = si.hardware
        private val os get() = si.operatingSystem

        private var prevTicks: LongArray? = null
        private val ticksLock = Any()

        private val CPU_STATS_REFRESH_INTERVAL_MS = 30000
    }

    @Volatile
    private var cachedCpu: Cpu? = null
    @Volatile
    private var lastCpuCalcTime: Long = 0L
    private val cpuStatsCalculationLock = Any()


    // Baseline for cpu stats calcs
    private var prevUpTimeMs: Long = 0L
    private var prevCpuTimeMs: Long = 0L

    /**
     * Gets or updates the cached CPU stats, depending on last update and cpu stats refresh interval
     */
    private fun getOrUpdateCpuStats(): Cpu {
        val currentCachedCpu = cachedCpu
        val currentLastCalcTime = lastCpuCalcTime

        if (currentCachedCpu != null && (System.currentTimeMillis() - currentLastCalcTime <= CPU_STATS_REFRESH_INTERVAL_MS)) {
            return currentCachedCpu
        }

        // Cache miss or stale, so update
        synchronized(cpuStatsCalculationLock) {
            // Check if another thread updated the cache while this thread waited for the lock.
            if (cachedCpu == null || (System.currentTimeMillis() - lastCpuCalcTime > CPU_STATS_REFRESH_INTERVAL_MS)) {
                cachedCpu = performCpuStatsCalculation()
                lastCpuCalcTime = System.currentTimeMillis()
            }
            return cachedCpu!!
        }
    }

    /**
     * Calculate of system and process CPU load.
     */
    private fun performCpuStatsCalculation(): Cpu {
        val systemLoad: Double
        synchronized(ticksLock) {
            val currentSystemTicks = hal.processor.systemCpuLoadTicks
            systemLoad = if (prevTicks == null) {
                0.0
            } else {
                hal.processor.getSystemCpuLoadBetweenTicks(prevTicks!!)
            }
            prevTicks = currentSystemTicks
        }

        var processLoadNormalized = 0.0
        val process = os.getProcess(os.processId)
        if (process == null) {
            log.warn("Cannot calculate CPU load: process is null for PID {}.", os.processId)
            // If process info is null, load will be 0 for this interval.
        } else {
            val currentProcessUptimeMs = process.upTime
            val currentProcessTotalCpuTimeMs = process.kernelTime + process.userTime

            // The first load of this will always be 0 and skip since we don't have a baseline yet
            if (prevUpTimeMs > 0L) {
                val uptimeDiffMs = currentProcessUptimeMs - prevUpTimeMs
                val cpuTimeDiffMs = currentProcessTotalCpuTimeMs - prevCpuTimeMs

                if (uptimeDiffMs > 0) {
                    // Process load relative to a single core
                    val singleCoreProcessLoad = cpuTimeDiffMs.toDouble() / uptimeDiffMs.toDouble()
                    processLoadNormalized = singleCoreProcessLoad / hal.processor.logicalProcessorCount.toDouble()
                }
            }

            prevUpTimeMs = currentProcessUptimeMs
            prevCpuTimeMs = currentProcessTotalCpuTimeMs
        }

        return Cpu(
            cores = hal.processor.logicalProcessorCount,
            systemLoad = systemLoad.coerceIn(0.0, 1.0).takeIf { it.isFinite() } ?: 0.0,
            lavalinkLoad = processLoadNormalized.coerceIn(0.0, 1.0).takeIf { it.isFinite() } ?: 0.0
        )
    }

    fun createTask(context: SocketContext): Runnable = Runnable {
        try {
            val stats = retrieveStats(context)
            context.sendMessage(Message.Serializer, Message.StatsEvent(stats))
        } catch (e: Exception) {
            log.error("Exception while sending stats", e)
        }
    }

    @GetMapping(value = ["/v4/stats"])
    fun getStats() = retrieveStats()

    fun retrieveStats(context: SocketContext? = null): Stats {
        val cpu = getOrUpdateCpuStats()

        val playersTotal = intArrayOf(0)
        val playersPlaying = intArrayOf(0)
        socketServer.contexts.forEach { socketContext ->
            playersTotal[0] += socketContext.players.size
            playersPlaying[0] += socketContext.playingPlayers.size
        }

        val uptime = System.currentTimeMillis() - Launcher.startTime

        // In bytes
        val runtime = Runtime.getRuntime()
        val mem = Memory(
            free = runtime.freeMemory(),
            used = runtime.totalMemory() - runtime.freeMemory(),
            allocated = runtime.totalMemory(),
            reservable = runtime.maxMemory()
        )

        var frameStats: FrameStats? = null
        if (context != null) {
            var playerCount = 0
            var totalSent = 0
            var totalNulled = 0
            for (player in context.playingPlayers) {
                val counter = player.audioLossCounter
                if (!counter.isDataUsable) continue
                playerCount++
                totalSent += counter.lastMinuteSuccess
                totalNulled += counter.lastMinuteLoss
            }

            // We can't divide by 0
            if (playerCount != 0) {
                val totalDeficit = playerCount *
                        AudioLossCounter.EXPECTED_PACKET_COUNT_PER_MIN -
                        (totalSent + totalNulled)

                frameStats = FrameStats(
                    totalSent / playerCount,
                    totalNulled / playerCount,
                    totalDeficit / playerCount
                )
            }
        }

        return StatsData(
            frameStats,
            playersTotal[0],
            playersPlaying[0],
            uptime,
            mem,
            cpu
        )
    }
}
