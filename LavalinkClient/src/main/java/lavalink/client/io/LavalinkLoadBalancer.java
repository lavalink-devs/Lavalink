package lavalink.client.io;

import net.dv8tion.jda.core.entities.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LavalinkLoadBalancer {

    private Lavalink lavalink;
    private Map<String, LavalinkSocket> socketMap = new ConcurrentHashMap<>();

    public LavalinkLoadBalancer(Lavalink lavalink) {
        this.lavalink = lavalink;
    }

    LavalinkSocket getSocket(String guildId) {
        return socketMap.computeIfAbsent(guildId, s -> determineBestSocket());
    }

    LavalinkSocket getSocket(Guild guild) {
        return getSocket(guild.getId());
    }

    private LavalinkSocket determineBestSocket() {
        LavalinkSocket leastPenalty = null;
        int record = Integer.MAX_VALUE;

        for (LavalinkSocket socket : lavalink.getNodes()) {
            int total = getPenalties(socket).getTotal();
            if (total < record) {
                leastPenalty = socket;
                record = total;
            }
        }

        if (leastPenalty == null)
            throw new IllegalStateException("No available nodes!");

        return leastPenalty;
    }

    public static Penalties getPenalties(LavalinkSocket socket) {
        return new Penalties(socket);
    }

    private static class Penalties {

        private int playerPenalty = 0;
        private int cpuPenalty = 0;
        private int deficitFramePenalty = 0;
        private int nullFramePenalty = 0;

        private Penalties(LavalinkSocket socket) {
            if (socket.stats == null) return;

            // This will serve as a rule of thumb. 1 playing player = 1 penalty point
            playerPenalty = socket.stats.getPlayingPlayers();

            // https://fred.moe/293.png
            cpuPenalty = (int) Math.pow(1.05d, 100 * socket.stats.getSystemLoad()) * 10 - 10;

            // Means we don't have any frame stats. This is normal for very young nodes
            if (socket.stats.getAvgFramesDeficitPerMinute() == -1) return;

            // https://fred.moe/UQJ.png
            deficitFramePenalty = (int) Math.pow(1.02d, 200 * (socket.stats.getAvgFramesDeficitPerMinute() / 3000)) * 300 - 300;
            nullFramePenalty = (int) Math.pow(1.02d, 200 * (socket.stats.getAvgFramesNulledPerMinute() / 3000)) * 300 - 300;
            nullFramePenalty *= 2;

            // Deficit frames are better than null frames, as deficit frames can be caused by the garbage collector
        }

        public int getPlayerPenalty() {
            return playerPenalty;
        }

        public int getCpuPenalty() {
            return cpuPenalty;
        }

        public int getDeficitFramePenalty() {
            return deficitFramePenalty;
        }

        public int getNullFramePenalty() {
            return nullFramePenalty;
        }

        public int getTotal() {
            return playerPenalty + cpuPenalty + deficitFramePenalty + nullFramePenalty;
        }

        @Override
        public String toString() {
            return "Penalties{" +
                    "playerPenalty=" + playerPenalty +
                    ", cpuPenalty=" + cpuPenalty +
                    ", deficitFramePenalty=" + deficitFramePenalty +
                    ", nullFramePenalty=" + nullFramePenalty +
                    '}';
        }
    }

}
