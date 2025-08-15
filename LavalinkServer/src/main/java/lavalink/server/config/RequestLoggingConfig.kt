package lavalink.server.config

import lavalink.server.io.RequestLoggingFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "logging.request")
@ConditionalOnProperty("logging.request.enabled", matchIfMissing = true)
data class RequestLoggingConfig(
    var includeClientInfo: Boolean = false,
    var includeHeaders: Boolean = false,
    var includeQueryString: Boolean = true,
    var includePayload: Boolean = true,
    var maxPayloadLength: Int = 10000,
    var beforeRequest: Boolean = false,
) {

    @Bean
    fun logFilter() = RequestLoggingFilter(this)

}