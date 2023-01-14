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
package lavalink.server.player

import com.sedmelluq.discord.lavaplayer.player.event.*

class AudioLossCounter : AudioEventListener {
    companion object {
        const val EXPECTED_PACKET_COUNT_PER_MIN = 60 * 1000 / 20 // 20ms packets
        private const val ACCEPTABLE_TRACK_SWITCH_TIME = 100 //ms
    }

    private var playingSince = Long.MAX_VALUE
    private var lastTrackStarted = Long.MAX_VALUE / 2
    private var lastTrackEnded = Long.MAX_VALUE

    private var curMinute: Long = 0
    private var curLoss = 0
    private var curSucc = 0

    var lastMinuteLoss = 0
        private set
    var lastMinuteSuccess = 0
        private set

    fun onLoss() {
        checkTime()
        curLoss++
    }

    fun onSuccess() {
        checkTime()
        curSucc++
    }

    val isDataUsable: Boolean
        get() {
            // Check that there isn't a significant gap in playback. If no track has ended yet, we can look past that
            if (lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME && lastTrackEnded != Long.MAX_VALUE) {
                return false
            }

            // Check that we have at least stats for the last minute
            val lastMin = System.currentTimeMillis() / 60000 - 1
            return playingSince < lastMin * 60000
        }

    private fun checkTime() {
        val actualMinute = System.currentTimeMillis() / 60000
        if (curMinute != actualMinute) {
            lastMinuteLoss = curLoss
            lastMinuteSuccess = curSucc
            curLoss = 0
            curSucc = 0
            curMinute = actualMinute
        }
    }

    override fun onEvent(event: AudioEvent) {
        when (event) {
            is PlayerPauseEvent,
            is TrackEndEvent,
            -> lastTrackEnded = System.currentTimeMillis()

            is PlayerResumeEvent,
            is TrackStartEvent,
            -> {
                lastTrackStarted = System.currentTimeMillis()
                if (lastTrackStarted - lastTrackEnded > ACCEPTABLE_TRACK_SWITCH_TIME || playingSince == Long.MAX_VALUE) {
                    playingSince = System.currentTimeMillis()
                    lastTrackEnded = Long.MAX_VALUE
                }
            }
        }
    }

    override fun toString(): String = buildString {
        append("AudioLossCounter{")
        append("lastLoss=$lastMinuteLoss, ")
        append("lastSucc=$lastMinuteSuccess, ")
        append("total=${lastMinuteSuccess + lastMinuteLoss}")
        append('}')
    }
}
