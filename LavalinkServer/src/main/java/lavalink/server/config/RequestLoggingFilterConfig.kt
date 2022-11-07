package lavalink.server.config

import lavalink.server.io.RequestLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RequestLoggingFilterConfig {

    @Bean
    fun logFilter() = RequestLoggingFilter()
}