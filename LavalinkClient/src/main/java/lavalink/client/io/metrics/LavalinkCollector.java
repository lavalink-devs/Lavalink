package lavalink.client.io.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lavalink.client.io.Lavalink;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.RemoteStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by napster on 22.01.18.
 * <p>
 * A prometheus collector for gathering and exposing lavalink metrics client side.
 */
@SuppressWarnings("unused")
public class LavalinkCollector extends Collector {

    private final Lavalink lavalink;

    public LavalinkCollector(@NonNull Lavalink lavalinkInstance) {
        this.lavalink = lavalinkInstance;
    }

    @Override
    public List<MetricFamilySamples> collect() {

        List<MetricFamilySamples> mfs = new ArrayList<>();
        List<String> labelNames = Collections.singletonList("node");


        GaugeMetricFamily players = new GaugeMetricFamily("lavalink_players_current",
                "Amount of players", labelNames);
        mfs.add(players);
        GaugeMetricFamily playingPlayers = new GaugeMetricFamily("lavalink_playing_players_current",
                "Amount of playing players", labelNames);
        mfs.add(playingPlayers);
        GaugeMetricFamily uptimeSeconds = new GaugeMetricFamily("lavalink_uptime_seconds",
                "Uptime of the node", labelNames);
        mfs.add(uptimeSeconds);


        GaugeMetricFamily memFree = new GaugeMetricFamily("lavalink_mem_free_bytes",
                "Amount of free memory", labelNames);
        mfs.add(memFree);
        GaugeMetricFamily memUsed = new GaugeMetricFamily("lavalink_mem_used_bytes",
                "Amount of used memory", labelNames);
        mfs.add(memUsed);
        GaugeMetricFamily memAllocated = new GaugeMetricFamily("lavalink_mem_allocated_bytes",
                "Amount of allocated memory", labelNames);
        mfs.add(memAllocated);
        GaugeMetricFamily memReservable = new GaugeMetricFamily("lavalink_mem_reservable_bytes",
                "Amount of reservable memory", labelNames);
        mfs.add(memReservable);

        GaugeMetricFamily cpuCores = new GaugeMetricFamily("lavalink_cpu_cores",
                "Amount of cpu cores", labelNames);
        mfs.add(cpuCores);
        GaugeMetricFamily systemLoad = new GaugeMetricFamily("lavalink_load_system",
                "Total load of the system", labelNames);
        mfs.add(systemLoad);
        GaugeMetricFamily lavalinkLoad = new GaugeMetricFamily("lavalink_load_lavalink",
                "Load caused by Lavalink", labelNames);
        mfs.add(lavalinkLoad);


        GaugeMetricFamily averageFramesSentPerMinute = new GaugeMetricFamily("lavalink_average_frames_sent_per_minute",
                "Average frames sent per minute", labelNames);
        mfs.add(averageFramesSentPerMinute);
        GaugeMetricFamily averageFramesNulledPerMinute = new GaugeMetricFamily("lavalink_average_frames_nulled_per_minute",
                "Average frames nulled per minute", labelNames);
        mfs.add(averageFramesNulledPerMinute);
        GaugeMetricFamily averageFramesDeficitPerMinute = new GaugeMetricFamily("lavalink_average_frames_deficit_per_minute",
                "Average frames deficit per minute", labelNames);
        mfs.add(averageFramesDeficitPerMinute);


        for (LavalinkSocket node : lavalink.getNodes()) {
            List<String> labels = Collections.singletonList(node.getName());
            RemoteStats stats = node.getStats();
            if (stats == null) {
                continue;
            }

            players.addMetric(labels, stats.getPlayers());
            playingPlayers.addMetric(labels, stats.getPlayingPlayers());
            uptimeSeconds.addMetric(labels, stats.getUptime() / 1000);

            memFree.addMetric(labels, stats.getMemFree());
            memUsed.addMetric(labels, stats.getMemUsed());
            memAllocated.addMetric(labels, stats.getMemAllocated());
            memReservable.addMetric(labels, stats.getMemReservable());

            cpuCores.addMetric(labels, stats.getCpuCores());
            systemLoad.addMetric(labels, stats.getSystemLoad());
            lavalinkLoad.addMetric(labels, stats.getLavalinkLoad());

            averageFramesSentPerMinute.addMetric(labels, stats.getAvgFramesSentPerMinute());
            averageFramesNulledPerMinute.addMetric(labels, stats.getAvgFramesNulledPerMinute());
            averageFramesDeficitPerMinute.addMetric(labels, stats.getAvgFramesDeficitPerMinute());
        }

        return mfs;
    }
}
