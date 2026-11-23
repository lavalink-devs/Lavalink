package lavalink.server.player

import dev.arbjerg.lavalink.api.AudioFrameListener
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException

class AudioFrameDispatcherTest {

    private class RecordingListener : AudioFrameListener {
        val frames = mutableListOf<Pair<Long, ByteArray>>()
        var readOnly = false

        override fun onAudioFrame(guildId: Long, sessionId: String, positionMs: Long, frame: ByteBuffer) {
            readOnly = frame.isReadOnly
            val copy = ByteArray(frame.remaining())
            frame.duplicate().get(copy)
            frames.add(positionMs to copy)
        }
    }

    private fun frame(vararg bytes: Byte): ByteBuffer {
        val buffer = ByteBuffer.allocate(bytes.size)
        buffer.put(bytes)
        buffer.flip()
        return buffer
    }

    @Test
    fun `dispatches the exact frame as a read-only view`() {
        val listener = RecordingListener()
        val dispatcher = AudioFrameDispatcher(listOf(listener))
        val buffer = frame(1, 2, 3, 4)

        dispatcher.frame(42L, "session", 1_234L, buffer)

        assertTrue(dispatcher.hasListeners)
        assertTrue(listener.readOnly)
        assertEquals(1, listener.frames.size)
        assertEquals(1_234L, listener.frames[0].first)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), listener.frames[0].second)
        assertEquals(0, buffer.position())
    }

    @Test
    fun `read-only view cannot mutate the Discord buffer`() {
        var readOnlyView: ByteBuffer? = null
        val listener = object : AudioFrameListener {
            override fun onAudioFrame(
                guildId: Long,
                sessionId: String,
                positionMs: Long,
                frame: ByteBuffer
            ) {
                readOnlyView = frame
            }
        }
        val dispatcher = AudioFrameDispatcher(listOf(listener))
        val buffer = frame(1, 2)

        dispatcher.frame(42L, "session", 0L, buffer)

        assertThrows(ReadOnlyBufferException::class.java) {
            readOnlyView!!.put(0, 9)
        }
    }

    @Test
    fun `no listeners is a no-op`() {
        val dispatcher = AudioFrameDispatcher(emptyList())
        val buffer = frame(1)

        assertFalse(dispatcher.hasListeners)
        dispatcher.frame(42L, "session", 0L, buffer)

        assertEquals(0, buffer.position())
    }

    @Test
    fun `a throwing listener does not stop the next listener`() {
        val healthy = RecordingListener()
        val broken = object : AudioFrameListener {
            override fun onAudioFrame(
                guildId: Long,
                sessionId: String,
                positionMs: Long,
                frame: ByteBuffer
            ) {
                throw IllegalStateException("plugin bug")
            }
        }
        val dispatcher = AudioFrameDispatcher(listOf(broken, healthy))

        dispatcher.frame(42L, "session", 10L, frame(7, 8))

        assertEquals(1, healthy.frames.size)
        assertArrayEquals(byteArrayOf(7, 8), healthy.frames[0].second)
    }
}
