package lavalink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Created by napster on 20.05.18.
 */
@Component
@ConfigurationProperties("metrics.prometheus")
data class MetricsPrometheusConfigProperties(
    var isEnabled: Boolean = false,
    var endpoint: String = ""
)
