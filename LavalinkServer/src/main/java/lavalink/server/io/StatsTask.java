package lavalink.server.io;

import lavalink.server.Launcher;
import lavalink.server.player.AudioLossCounter;
import lavalink.server.player.Player;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public class StatsTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(StatsTask.class);

    private SocketContext context;

    StatsTask(SocketContext context) {
        this.context = context;
    }

    @Override
    public void run() {
        log.info("Sending stats");

        JSONObject out = new JSONObject();
        out.put("op", "stats");
        out.put("players", context.getPlayers().size());
        out.put("playingPlayers", context.getPlayingPlayers().size());
        out.put("uptime", System.currentTimeMillis() - Launcher.startTime);

        // In bytes
        JSONObject mem = new JSONObject();
        mem.put("free", Runtime.getRuntime().freeMemory());
        mem.put("used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        mem.put("allocated", Runtime.getRuntime().totalMemory());
        mem.put("reservable", Runtime.getRuntime().maxMemory());
        out.put("memory", mem);

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        OSProcess p = os.getProcess(os.getProcessId());

        JSONObject cpu = new JSONObject();
        cpu.put("cores", Runtime.getRuntime().availableProcessors());
        cpu.put("systemLoad", hal.getProcessor().getSystemCpuLoad());
        cpu.put("lavalinkLoad", (p.getKernelTime() + p.getUserTime()) / p.getUpTime());
        out.put("cpu", cpu);

        int totalSent = 0;
        int totalNulled = 0;
        int players = 0;

        for (Player player : context.getPlayingPlayers()) {
            AudioLossCounter counter = player.getAudioLossCounter();
            if (!counter.isDataUsable()) continue;

            players++;
            totalSent += counter.getLastMinuteSuccess();
            totalNulled += counter.getLastMinuteLoss();
        }

        int totalDeficit = players * AudioLossCounter.EXPECTED_PACKET_COUNT_PER_MIN
                - (totalSent + totalNulled);

        // We can't divide by 0
        if (players != 0) {
            JSONObject frames = new JSONObject();
            frames.put("sent", totalSent / players);
            frames.put("nulled", totalNulled / players);
            frames.put("deficit", totalDeficit / players);
            out.put("frameStats", frames);
        }

        context.getSocket().send(out.toString());
    }

}
