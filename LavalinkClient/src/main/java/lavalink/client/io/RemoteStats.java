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

import org.json.JSONObject;

public class RemoteStats {

    private final JSONObject json;
    private final int players;
    private final int playingPlayers;
    private final long uptime;

    // In bytes
    private final int memFree;
    private final int memUsed;
    private final int memAllocated;
    private final int memReservable;

    private final int cpuCores;
    private final double systemLoad;
    private final double lavalinkLoad;

    private int avgFramesSentPerMinute = -1;
    private int avgFramesNulledPerMinute = -1;
    private int avgFramesDeficitPerMinute = -1;

    RemoteStats(JSONObject json) {
        this.json = json;
        players = json.getInt("players");
        playingPlayers = json.getInt("playingPlayers");
        uptime = json.getLong("uptime");

        memFree = json.getJSONObject("memory").getInt("free");
        memUsed = json.getJSONObject("memory").getInt("used");
        memAllocated = json.getJSONObject("memory").getInt("allocated");
        memReservable = json.getJSONObject("memory").getInt("reservable");

        cpuCores = json.getJSONObject("cpu").getInt("cores");
        systemLoad = json.getJSONObject("cpu").getDouble("systemLoad");
        lavalinkLoad = json.getJSONObject("cpu").getDouble("lavalinkLoad");

        JSONObject frames = json.optJSONObject("frameStats");

        if (frames != null) {
            avgFramesSentPerMinute = frames.getInt("sent");
            avgFramesNulledPerMinute = frames.getInt("nulled");
            avgFramesDeficitPerMinute = frames.getInt("deficit");
        }
    }

    public JSONObject getAsJson() {
        return json;
    }

    public int getPlayers() {
        return players;
    }

    public int getPlayingPlayers() {
        return playingPlayers;
    }

    public long getUptime() {
        return uptime;
    }

    public int getMemFree() {
        return memFree;
    }

    public int getMemUsed() {
        return memUsed;
    }

    public int getMemAllocated() {
        return memAllocated;
    }

    public int getMemReservable() {
        return memReservable;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public double getLavalinkLoad() {
        return lavalinkLoad;
    }

    public int getAvgFramesSentPerMinute() {
        return avgFramesSentPerMinute;
    }

    public int getAvgFramesNulledPerMinute() {
        return avgFramesNulledPerMinute;
    }

    public int getAvgFramesDeficitPerMinute() {
        return avgFramesDeficitPerMinute;
    }

    @Override
    public String toString() {
        return "RemoteStats{" +
                "players=" + players +
                ", playingPlayers=" + playingPlayers +
                ", uptime=" + uptime +
                ", memFree=" + memFree +
                ", memUsed=" + memUsed +
                ", memAllocated=" + memAllocated +
                ", memReservable=" + memReservable +
                ", cpuCores=" + cpuCores +
                ", systemLoad=" + systemLoad +
                ", lavalinkLoad=" + lavalinkLoad +
                ", avgFramesSentPerMinute=" + avgFramesSentPerMinute +
                ", avgFramesNulledPerMinute=" + avgFramesNulledPerMinute +
                ", avgFramesDeficitPerMinute=" + avgFramesDeficitPerMinute +
                '}';
    }
}
