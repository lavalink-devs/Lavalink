package lavalink.server.metrics

import io.prometheus.client.Collector
import io.prometheus.client.GaugeMetricFamily
import lavalink.server.io.StatsCollector
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConditionalOnProperty("metrics.prometheus.enabled") // Only create this if prometheus is enabled
class LavalinkStatsCollector(
    private val statsProvider: StatsCollector
) : Collector(), Collector.Describable {

    init {
        // Register with the default Prometheus registry
        this.register<LavalinkStatsCollector>()
        log.info("Lavalink custom stats collector registered with Prometheus.")
    }

    override fun collect(): MutableList<MetricFamilySamples> {
        val mfs: MutableList<MetricFamilySamples> = ArrayList<MetricFamilySamples>()

        // Since we don't have a context, framestats will be null and we can ignore them
        val stats = statsProvider.retrieveStats(null)

        val playersGauge = GaugeMetricFamily(
            PLAYERS_METRIC_NAME,
            PLAYERS_HELP,
            mutableListOf<String?>()
        )
        playersGauge.addMetric(mutableListOf<String?>(), stats.players.toDouble())
        mfs.add(playersGauge)

        val playingPlayersGauge = GaugeMetricFamily(
            PLAYING_PLAYERS_METRIC_NAME,
            PLAYING_PLAYERS_HELP,
            mutableListOf<String?>()
        )
        playingPlayersGauge.addMetric(mutableListOf<String?>(), stats.playingPlayers.toDouble())
        mfs.add(playingPlayersGauge)

        val uptimeGauge = GaugeMetricFamily(
            UPTIME_METRIC_NAME,
            UPTIME_HELP,
            mutableListOf<String?>()
        )
        uptimeGauge.addMetric(mutableListOf<String?>(), stats.uptime.toDouble())
        mfs.add(uptimeGauge)

        val memory = stats.memory
        val memFreeGauge = GaugeMetricFamily(
            MEMORY_FREE_METRIC_NAME,
            "$MEMORY_HELP (Free)",
            mutableListOf<String?>()
        )
        memFreeGauge.addMetric(mutableListOf<String?>(), memory.free.toDouble())
        mfs.add(memFreeGauge)

        val memUsedGauge = GaugeMetricFamily(
            MEMORY_USED_METRIC_NAME,
            "$MEMORY_HELP (Used)",
            mutableListOf<String?>()
        )
        memUsedGauge.addMetric(mutableListOf<String?>(), memory.used.toDouble())
        mfs.add(memUsedGauge)

        val memAllocatedGauge = GaugeMetricFamily(
            MEMORY_ALLOCATED_METRIC_NAME,
            "$MEMORY_HELP (Allocated)",
            mutableListOf<String?>()
        )
        memAllocatedGauge.addMetric(mutableListOf<String?>(), memory.allocated.toDouble())
        mfs.add(memAllocatedGauge)

        val memReservableGauge = GaugeMetricFamily(
            MEMORY_RESERVABLE_METRIC_NAME,
            "$MEMORY_HELP (Reservable)",
            mutableListOf<String?>()
        )
        memReservableGauge.addMetric(mutableListOf<String?>(), memory.reservable.toDouble())
        mfs.add(memReservableGauge)

        val cpu = stats.cpu
        val cpuCoresGauge = GaugeMetricFamily(
            CPU_CORES_METRIC_NAME,
            "$CPU_HELP (Cores)",
            mutableListOf<String?>()
        )
        cpuCoresGauge.addMetric(mutableListOf<String?>(), cpu.cores.toDouble())
        mfs.add(cpuCoresGauge)

        val cpuSystemLoadGauge = GaugeMetricFamily(
            CPU_SYSTEM_LOAD_METRIC_NAME,
            "$CPU_HELP (System Load)",
            mutableListOf<String?>()
        )
        cpuSystemLoadGauge.addMetric(mutableListOf<String?>(), cpu.systemLoad)
        mfs.add(cpuSystemLoadGauge)

        val cpuLavalinkLoadGauge = GaugeMetricFamily(
            CPU_LAVALINK_LOAD_METRIC_NAME,
            "$CPU_HELP (LL Load)",
            mutableListOf<String?>()
        )
        cpuLavalinkLoadGauge.addMetric(mutableListOf<String?>(), cpu.lavalinkLoad)
        mfs.add(cpuLavalinkLoadGauge)

        return mfs
    }

    override fun describe(): MutableList<MetricFamilySamples> {
        // Used by prometheus for validation
        return listOf<MetricFamilySamples>(
            GaugeMetricFamily(PLAYERS_METRIC_NAME, PLAYERS_HELP, mutableListOf<String?>()),
            GaugeMetricFamily(PLAYING_PLAYERS_METRIC_NAME, PLAYING_PLAYERS_HELP, mutableListOf<String?>()),
            GaugeMetricFamily(UPTIME_METRIC_NAME, UPTIME_HELP, mutableListOf<String?>()),
            GaugeMetricFamily(MEMORY_FREE_METRIC_NAME, "$MEMORY_HELP (Free)", mutableListOf<String?>()),
            GaugeMetricFamily(MEMORY_USED_METRIC_NAME, "$MEMORY_HELP (Used)", mutableListOf<String?>()),
            GaugeMetricFamily(MEMORY_ALLOCATED_METRIC_NAME, "$MEMORY_HELP (Allocated)", mutableListOf<String?>()),
            GaugeMetricFamily(MEMORY_RESERVABLE_METRIC_NAME, "$MEMORY_HELP (Reservable)", mutableListOf<String?>()),
            GaugeMetricFamily(CPU_CORES_METRIC_NAME, "$CPU_HELP (Cores)", mutableListOf<String?>()),
            GaugeMetricFamily(CPU_SYSTEM_LOAD_METRIC_NAME, "$CPU_HELP (System Load)", mutableListOf<String?>()),
            GaugeMetricFamily(CPU_LAVALINK_LOAD_METRIC_NAME, "$CPU_HELP (Lavalink Load)", mutableListOf<String?>())
        ) as MutableList<MetricFamilySamples>
    }

    companion object {
        private val log = LoggerFactory.getLogger(LavalinkStatsCollector::class.java)

        private const val PREFIX = "lavalink_" // prefix for all ll metrics

        private const val PLAYERS_METRIC_NAME = PREFIX + "players_total"
        private const val PLAYERS_HELP = "Total number of players connected."
        private const val PLAYING_PLAYERS_METRIC_NAME = PREFIX + "playing_players_total"
        private const val PLAYING_PLAYERS_HELP = "Number of players currently playing audio."
        private const val UPTIME_METRIC_NAME = PREFIX + "uptime_milliseconds"
        private const val UPTIME_HELP = "Uptime of the node in milliseconds."

        private const val MEMORY_FREE_METRIC_NAME = PREFIX + "memory_free_bytes"
        private const val MEMORY_USED_METRIC_NAME = PREFIX + "memory_used_bytes"
        private const val MEMORY_ALLOCATED_METRIC_NAME = PREFIX + "memory_allocated_bytes"
        private const val MEMORY_RESERVABLE_METRIC_NAME = PREFIX + "memory_reservable_bytes"
        private const val MEMORY_HELP = "Memory statistics in bytes."

        private const val CPU_CORES_METRIC_NAME = PREFIX + "cpu_cores"
        private const val CPU_SYSTEM_LOAD_METRIC_NAME = PREFIX + "cpu_system_load_percentage"
        private const val CPU_LAVALINK_LOAD_METRIC_NAME = PREFIX + "cpu_lavalink_load_percentage"
        private const val CPU_HELP = "CPU statistics."
    }
}