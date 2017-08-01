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

import com.sedmelluq.discord.lavaplayer.tools.DaemonThreadFactory;
import com.sedmelluq.discord.lavaplayer.tools.ExecutorTools;
import com.sedmelluq.discord.lavaplayer.udpqueue.natives.UdpQueueManager;
import net.dv8tion.jda.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.audio.factory.IPacketProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class NativeAudioSendFactory implements IAudioSendFactory {
    private static final int BUFFER_DURATION = 400;
    private static final int PACKET_INTERVAL = 20;
    private static final int MAXIMUM_PACKET_SIZE = 4096;

    private final AtomicLong identifierCounter = new AtomicLong();
    private final KeySetView<NativeAudioSendSystem, Boolean> systems = ConcurrentHashMap.newKeySet();
    private final Object lock = new Object();
    private volatile UdpQueueManager queueManager;
    private ScheduledExecutorService scheduler;

    private void initialiseQueueManager() {
        scheduler = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory("native-udp"));
        queueManager = new UdpQueueManager(BUFFER_DURATION / PACKET_INTERVAL,
                TimeUnit.MILLISECONDS.toNanos(PACKET_INTERVAL), MAXIMUM_PACKET_SIZE);

        scheduler.scheduleAtFixedRate(this::populateQueues, 0, 40, TimeUnit.MILLISECONDS);

        Thread thread = new Thread(queueManager::process);
        thread.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);
        thread.setDaemon(true);
        thread.start();
    }

    private void shutdownQueueManager() {
        queueManager.close();
        queueManager = null;

        ExecutorTools.shutdownExecutor(scheduler, "native udp queue populator");
    }

    @Override
    public IAudioSendSystem createSendSystem(IPacketProvider packetProvider) {
        return new NativeAudioSendSystem(identifierCounter.incrementAndGet(), this, packetProvider);
    }

    void addInstance(NativeAudioSendSystem system) {
        synchronized (lock) {
            systems.add(system);

            if (queueManager == null) {
                initialiseQueueManager();
            }
        }
    }

    void removeInstance(NativeAudioSendSystem system) {
        synchronized (lock) {
            if (systems.remove(system) && systems.isEmpty() && queueManager != null) {
                shutdownQueueManager();
            }
        }
    }

    private void populateQueues() {
        UdpQueueManager manager = queueManager; /* avoid getfield opcode */

        if (manager != null) {
            for (NativeAudioSendSystem system : systems) {
                system.populateQueue(manager);
            }
        }
    }
}
