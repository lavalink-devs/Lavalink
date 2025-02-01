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

package lavalink.server.config

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "lavalink.server")
@Component
class ServerConfig {
    var password: String? = null
    var isNonAllocatingFrameBuffer = false
    var bufferDurationMs: Int? = null
    var frameBufferDurationMs: Int? = null
    var opusEncodingQuality: Int? = null
    var resamplingQuality: ResamplingQuality? = null
    var trackStuckThresholdMs: Long? = null
    var useSeekGhosting: Boolean? = null
    var youtubePlaylistLoadLimit: Int? = null
    var playerUpdateInterval: Int = 5
    var isGcWarnings = true
    var isYoutubeSearchEnabled = true
    var isSoundcloudSearchEnabled = true
    var ratelimit: RateLimitConfig? = null
    var youtubeConfig: YoutubeConfig? = null
    var httpConfig: HttpConfig? = null
    var filters: Map<String, Boolean> = mapOf()
    var timeouts: TimeoutsConfig? = null
}

class TimeoutsConfig {
    var connectTimeoutMs: Int = 3000
    var connectionRequestTimeoutMs: Int = 3000
    var socketTimeoutMs: Int = 3000
}
