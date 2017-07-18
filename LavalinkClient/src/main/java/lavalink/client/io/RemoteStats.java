package lavalink.client.io;

import org.json.JSONObject;

public class RemoteStats {

    private int players;
    private int playingPlayers;
    private long uptime;

    // In bytes
    private int memFree;
    private int memUsed;
    private int memAllocated;
    private int memReservable;

    private int cpuCores;
    private double systemLoad;
    private double lavalinkLoad;

    private int avgFramesSentPerMinute = -1;
    private int avgFramesNulledPerMinute = -1;
    private int avgFramesDeficitPerMinute = -1;

    RemoteStats(JSONObject json) {
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
