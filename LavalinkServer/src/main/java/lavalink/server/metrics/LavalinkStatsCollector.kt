package lavalink.server.metrics

import io.prometheus.client.Collector
import io.prometheus.client.GaugeMetricFamily
import lavalink.server.io.StatsCollector
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("metrics.prometheus.enabled") // Only create this if prometheus is enabled
class LavalinkStatsCollector(
    private val statsProvider: StatsCollector
) : Collector(), Collector.Describable {

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

    init {
        // Register with the default Prometheus registry
        this.register<LavalinkStatsCollector>()
        log.info("Lavalink custom stats collector registered with Prometheus.")
    }

    override fun collect(): MutableList<MetricFamilySamples> {
        // Since we don't have a context, framestats will be null and we can ignore them
        val stats = statsProvider.retrieveStats(null)
        val memory = stats.memory
        val cpu = stats.cpu

        return mutableListOf(
            GaugeMetricFamily(
                PLAYERS_METRIC_NAME,
                PLAYERS_HELP,
                stats.players.toDouble()
            ),
            GaugeMetricFamily(
                PLAYING_PLAYERS_METRIC_NAME,
                PLAYING_PLAYERS_HELP,
                stats.playingPlayers.toDouble()
            ),
            GaugeMetricFamily(
                UPTIME_METRIC_NAME,
                UPTIME_HELP,
                stats.uptime.toDouble()
            ),
            GaugeMetricFamily(
                MEMORY_FREE_METRIC_NAME,
                "$MEMORY_HELP (Free)",
                memory.free.toDouble()
            ),
            GaugeMetricFamily(
                MEMORY_USED_METRIC_NAME,
                "$MEMORY_HELP (Used)",
                memory.used.toDouble()
            ),
            GaugeMetricFamily(
                MEMORY_ALLOCATED_METRIC_NAME,
                "$MEMORY_HELP (Allocated)",
                memory.allocated.toDouble()
            ),
            GaugeMetricFamily(
                MEMORY_RESERVABLE_METRIC_NAME,
                "$MEMORY_HELP (Reservable)",
                memory.reservable.toDouble()
            ),
            GaugeMetricFamily(
                CPU_CORES_METRIC_NAME,
                "$CPU_HELP (Cores)",
                cpu.cores.toDouble()
            ),
            GaugeMetricFamily(
                CPU_SYSTEM_LOAD_METRIC_NAME,
                "$CPU_HELP (System Load)",
                cpu.systemLoad
            ),
            GaugeMetricFamily(
                CPU_LAVALINK_LOAD_METRIC_NAME,
                "$CPU_HELP (LL Load)",
                cpu.lavalinkLoad
            )
        )
    }

    override fun describe(): MutableList<MetricFamilySamples> {
        // Used by prometheus for validation
        return mutableListOf(
            GaugeMetricFamily(PLAYERS_METRIC_NAME, PLAYERS_HELP, mutableListOf()),
            GaugeMetricFamily(PLAYING_PLAYERS_METRIC_NAME, PLAYING_PLAYERS_HELP, mutableListOf()),
            GaugeMetricFamily(UPTIME_METRIC_NAME, UPTIME_HELP, mutableListOf()),
            GaugeMetricFamily(MEMORY_FREE_METRIC_NAME, "$MEMORY_HELP (Free)", mutableListOf()),
            GaugeMetricFamily(MEMORY_USED_METRIC_NAME, "$MEMORY_HELP (Used)", mutableListOf()),
            GaugeMetricFamily(MEMORY_ALLOCATED_METRIC_NAME, "$MEMORY_HELP (Allocated)", mutableListOf()),
            GaugeMetricFamily(MEMORY_RESERVABLE_METRIC_NAME, "$MEMORY_HELP (Reservable)", mutableListOf()),
            GaugeMetricFamily(CPU_CORES_METRIC_NAME, "$CPU_HELP (Cores)", mutableListOf()),
            GaugeMetricFamily(CPU_SYSTEM_LOAD_METRIC_NAME, "$CPU_HELP (System Load)", mutableListOf()),
            GaugeMetricFamily(CPU_LAVALINK_LOAD_METRIC_NAME, "$CPU_HELP (Lavalink Load)", mutableListOf())
        )
    }
}