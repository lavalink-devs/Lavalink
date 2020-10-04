package lavalink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by napster on 20.05.18.
 */
@Component
@ConfigurationProperties(prefix = "sentry")
class SentryConfigProperties {
    var dsn = ""
    var environment = ""
    var tags: Map<String, String> = HashMap()
}