package lavalink.server.metrics

import io.prometheus.client.Counter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("metrics.prometheus.enabled")
class SearchMetrics {
    
    companion object {
        private val searchCounter: Counter = Counter.build()
            .name("lavalink_track_loads_total")
            .help("Total number of tracks searched by source")
            .labelNames("source")
            .register()

        private val playCounter: Counter = Counter.build()
            .name("lavalink_tracks_played_total")
            .help("Total number of tracks played by source")
            .labelNames("source", "guild_id")
            .register()

        private val loadResultCounter: Counter = Counter.build()
            .name("lavalink_track_load_results_total")
            .help("Total number of track load results by source and result type")
            .labelNames("source", "result")
            .register()
    }
    
    fun recordSearch(source: String) {
        searchCounter.labels(source).inc()
    }

    fun recordPlay(source: String, guildId: String) {
        playCounter.labels(source, guildId).inc()
    }

    fun recordLoadResult(source: String, result: String) {
        loadResultCounter.labels(source, result).inc()
    }
}

