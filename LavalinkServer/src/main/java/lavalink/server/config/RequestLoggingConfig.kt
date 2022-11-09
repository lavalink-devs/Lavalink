package lavalink.server.config

import lavalink.server.io.RequestLoggingFilter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter

@Configuration
@ConfigurationProperties(prefix = "logging.request")
data class RequestLoggingConfig(
    var enabled: Boolean = true,
    var includeClientInfo: Boolean = true,
    var includeHeaders: Boolean = false,
    var includeQueryString: Boolean = true,
    var includePayload: Boolean = true,
    var maxPayloadLength: Int = 10000,
) {

    @Bean
    fun logFilter(): Filter {
        if (!enabled) return Filter { _, _, _ -> }

        return RequestLoggingFilter(this)
    }
}