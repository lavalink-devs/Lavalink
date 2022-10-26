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

import dev.arbjerg.lavalink.protocol.Message
import dev.arbjerg.lavalink.protocol.Message.Cpu
import dev.arbjerg.lavalink.protocol.Message.FrameStats
import lavalink.server.Launcher.startTime
import lavalink.server.player.AudioLossCounter
import org.slf4j.LoggerFactory
import oshi.SystemInfo
import java.util.function.Consumer

class StatsTask(
    private val context: SocketContext,
    private val socketServer: SocketServer
) : Runnable {

    companion object {
        private val log = LoggerFactory.getLogger(StatsTask::class.java)
        private val si = SystemInfo()
        private val hal = si.hardware
        private var prevTicks: LongArray? = null
    }

    override fun run() {
        try {
            sendStats()
        } catch (e: Exception) {
            log.error("Exception while sending stats", e)
        }
    }

    private fun sendStats() {
        if (context.sessionPaused) return
        val playersTotal = intArrayOf(0)
        val playersPlaying = intArrayOf(0)
        socketServer.contexts.forEach(Consumer { socketContext: SocketContext ->
            playersTotal[0] += socketContext.players.size
            playersPlaying[0] += socketContext.playingPlayers.size
        })
        val uptime = System.currentTimeMillis() - startTime

        // In bytes
        val mem = Message.Memory(
            Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(),
            Runtime.getRuntime().totalMemory(),
            Runtime.getRuntime().maxMemory()
        )

        // prevTicks will be null so set it to a value.
        if (prevTicks == null) {
            prevTicks = hal.processor.systemCpuLoadTicks
        }
        // Set new prevTicks to current value for more accurate baseline, and checks in next schedule.
        prevTicks = hal.processor.systemCpuLoadTicks
        var load = processRecentCpuUsage
        if (!java.lang.Double.isFinite(load)) load = 0.0
        val cpu = Cpu(
            Runtime.getRuntime().availableProcessors(),
            hal.processor.getSystemCpuLoadBetweenTicks(prevTicks),
            load
        )
        var totalSent = 0
        var totalNulled = 0
        var players = 0
        for (player in context.playingPlayers) {
            val counter = player.audioLossCounter
            if (!counter.isDataUsable) continue
            players++
            totalSent += counter.lastMinuteSuccess
            totalNulled += counter.lastMinuteLoss
        }
        val totalDeficit = players * AudioLossCounter.EXPECTED_PACKET_COUNT_PER_MIN - (totalSent + totalNulled)
        var frameStats: FrameStats? = null
        // We can't divide by 0
        if (players != 0) {
            frameStats = FrameStats(
                (totalSent / players).toLong(),
                (totalNulled / players).toLong(),
                (totalDeficit / players).toLong()
            )
        }
        context.send(
            Message.Stats(
                frameStats,
                players,
                playersPlaying[0],
                uptime,
                mem,
                cpu
            )
        )
    }

    private var uptime = 0.0
    private var cpuTime = 0.0

    // Record for next invocation
    private val processRecentCpuUsage: Double
        get() {
            val output: Double
            val hal = si.hardware
            val os = si.operatingSystem
            val p = os.getProcess(os.processId)
            output = if (cpuTime != 0.0) {
                val uptimeDiff = p.upTime - uptime
                val cpuDiff = p.kernelTime + p.userTime - cpuTime
                cpuDiff / uptimeDiff
            } else {
                (p.kernelTime + p.userTime).toDouble() / p.userTime.toDouble()
            }

            // Record for next invocation
            uptime = p.upTime.toDouble()
            cpuTime = (p.kernelTime + p.userTime).toDouble()
            return output / hal.processor.logicalProcessorCount
        }

}