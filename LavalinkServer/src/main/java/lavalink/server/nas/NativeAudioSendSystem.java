/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
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

package lavalink.server.nas;

import com.sedmelluq.discord.lavaplayer.udpqueue.natives.UdpQueueManager;
import net.dv8tion.jda.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.audio.factory.IPacketProvider;

import java.net.DatagramPacket;

public class NativeAudioSendSystem implements IAudioSendSystem {
    private final long queueKey;
    private final NativeAudioSendFactory audioSendSystem;
    private final IPacketProvider packetProvider;

    public NativeAudioSendSystem(long queueKey, NativeAudioSendFactory audioSendSystem, IPacketProvider packetProvider) {
        this.queueKey = queueKey;
        this.audioSendSystem = audioSendSystem;
        this.packetProvider = packetProvider;
    }

    @Override
    public void start() {
        audioSendSystem.addInstance(this);
    }

    @Override
    public void shutdown() {
        audioSendSystem.removeInstance(this);
    }

    void populateQueue(UdpQueueManager queueManager) {
        int remaining = queueManager.getRemainingCapacity(queueKey);
        boolean emptyQueue = queueManager.getCapacity() - remaining > 0;

        for (int i = 0; i < remaining; i++) {
            DatagramPacket packet = packetProvider.getNextPacket(emptyQueue);

            if (packet == null || !queueManager.queuePacket(queueKey, packet)) {
                break;
            }
        }
    }
}
