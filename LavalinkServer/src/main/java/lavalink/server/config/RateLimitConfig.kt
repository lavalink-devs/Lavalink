package lavalink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "lavalink.server.ratelimit")
@Component
class RateLimitConfig {

    var ipVersion = "6"
    var ipBlock = ""
    var excludedIps: List<String> = emptyList()
    var stategy = "RotateOnBan" // LoadBalance

}