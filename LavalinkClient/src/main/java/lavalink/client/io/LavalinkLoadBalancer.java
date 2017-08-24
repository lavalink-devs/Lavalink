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

package lavalink.client.io;

import net.dv8tion.jda.core.entities.Guild;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class LavalinkLoadBalancer {

    private Lavalink lavalink;
    private Map<String, Optional<LavalinkSocket>> socketMap = new ConcurrentHashMap<>();

    LavalinkLoadBalancer(Lavalink lavalink) {
        this.lavalink = lavalink;
    }

    LavalinkSocket getSocket(String guildId) {
        return socketMap.computeIfAbsent(guildId, s -> Optional.ofNullable(determineBestSocket())).orElse(null);
    }

    LavalinkSocket getSocket(Guild guild) {
        return getSocket(guild.getId());
    }

    private LavalinkSocket determineBestSocket() {
        LavalinkSocket leastPenalty = null;
        int record = Integer.MAX_VALUE ;

        for (LavalinkSocket socket : lavalink.getNodes()) {
            int total = getPenalties(socket).getTotal();
            if (total < record) {
                leastPenalty = socket;
                record = total;
            }
        }

        if (leastPenalty == null)
            throw new IllegalStateException("No available nodes!");

        if (!leastPenalty.isOpen()) return null;

        return leastPenalty;
    }

    void onNodeDisconnect(LavalinkSocket disconnected) {
        socketMap.replaceAll((guildId, socket) -> {
            boolean isAffected = disconnected.equals(socket.orElse(null));

            LavalinkSocket newSocket = isAffected ? determineBestSocket() : socket.orElse(null);
            if (isAffected)
                lavalink.getPlayer(guildId).setSocket(newSocket);

            return Optional.ofNullable(newSocket);
        });
    }

    void onNodeConnect(LavalinkSocket connected) {
        for (String guildId : socketMap.keySet()) {
            if(!socketMap.get(guildId).isPresent()) {
                socketMap.put(guildId, Optional.of(connected));
                lavalink.getPlayer(guildId).setSocket(connected);
            }
        }
    }

    public static Penalties getPenalties(LavalinkSocket socket) {
        return new Penalties(socket);
    }

    @SuppressWarnings("unused")
    public static class Penalties {

        private LavalinkSocket socket;
        private int playerPenalty = 0;
        private int cpuPenalty = 0;
        private int deficitFramePenalty = 0;
        private int nullFramePenalty = 0;

        private Penalties(LavalinkSocket socket) {
            this.socket = socket;
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
            if (!socket.isOpen()) return Integer.MAX_VALUE - 1;

            return playerPenalty + cpuPenalty + deficitFramePenalty + nullFramePenalty;
        }

        @Override
        public String toString() {
            if (!socket.isOpen()) return "Penalties{" +
                    "unavailable=" + (Integer.MAX_VALUE - 1) +
                    '}';

            return "Penalties{" +
                    "total=" + getTotal() +
                    ", playerPenalty=" + playerPenalty +
                    ", cpuPenalty=" + cpuPenalty +
                    ", deficitFramePenalty=" + deficitFramePenalty +
                    ", nullFramePenalty=" + nullFramePenalty +
                    '}';
        }
    }

}
