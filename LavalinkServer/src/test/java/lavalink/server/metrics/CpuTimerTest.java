package lavalink.server.metrics;


import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by napster on 06.05.19.
 */
public class CpuTimerTest {

    @Test
    public void getProcessRecentCpuUsage() {
        CpuTimer cpuTimer = new CpuTimer();
        cpuTimer.getProcessRecentCpuUsage(); //first call may be negative
        assertTrue(cpuTimer.getProcessRecentCpuUsage() > CpuTimer.ERROR);
    }

    @Test
    public void getSystemRecentCpuUsage() {
        CpuTimer cpuTimer = new CpuTimer();
        cpuTimer.getSystemRecentCpuUsage(); //first call may be negative
        assertTrue(cpuTimer.getSystemRecentCpuUsage() > CpuTimer.ERROR);
    }
}