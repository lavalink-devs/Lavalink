package lavalink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "lavalink.server.ratelimit")
@Component
class RateLimitConfig {

    @Deprecated(message = "Use `ipBlocks` list instead")
    var ipBlock = ""
    var ipBlocks: List<String> = emptyList()
    var excludedIps: List<String> = emptyList()
    var strategy = "RotateOnBan" // RotateOnBan | LoadBalance
    var searchTriggersFail = true

}