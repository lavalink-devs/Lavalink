package lavalink.server.metrics;


import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by napster on 06.05.19.
 */
public class CpuTimerTest {

    @Test
    public void getProcessRecentCpuUsage() throws InterruptedException {
        CpuTimer cpuTimer = new CpuTimer();
        if (cpuTimer.getProcessRecentCpuUsage() <= CpuTimer.ERROR) {
            //first call may be negative and/or the specific JVM requires a bit more time to populate
            Thread.sleep(1000);
        }
        assertTrue(cpuTimer.getProcessRecentCpuUsage() > CpuTimer.ERROR);
    }

    @Test
    public void getSystemRecentCpuUsage() throws InterruptedException {
        CpuTimer cpuTimer = new CpuTimer();
        if (cpuTimer.getSystemRecentCpuUsage() <= CpuTimer.ERROR) {
            //first call may be negative and/or the specific JVM requires a bit more time to populate
            Thread.sleep(1000);
        }
        assertTrue(cpuTimer.getSystemRecentCpuUsage() > CpuTimer.ERROR);
    }
}