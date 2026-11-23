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

package dev.arbjerg.lavalink.api

import java.nio.ByteBuffer

/**
 * Receives the encoded audio frames Lavalink sends to Discord.
 *
 * Implement this interface as a Spring bean. Lavalink discovers every bean of
 * this type and dispatches to them; with no beans registered, the audio path
 * does no extra work and allocates nothing per frame.
 *
 * Callbacks run on the audio thread. They must not block, perform I/O, or call
 * Discord. [frame] is a read-only view that is only valid for the duration of
 * the call; copy the bytes if they are needed afterwards.
 */
interface AudioFrameListener {
    /**
     * Called once for every encoded frame written to Discord.
     *
     * @param guildId guild owning the player
     * @param sessionId Lavalink session id
     * @param positionMs playing track position in milliseconds at dispatch time.
     *   This is the player clock, not a per-frame timestamp.
     * @param frame read-only encoded Opus frame
     */
    fun onAudioFrame(guildId: Long, sessionId: String, positionMs: Long, frame: ByteBuffer)
}
