package lavalink.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Created by napster on 05.03.18.
 */
@ConfigurationProperties(prefix = "lavalink.server.sources")
@Component
data class AudioSourcesConfig(
    var isYoutube: Boolean = true,
    var isBandcamp: Boolean = true,
    var isSoundcloud: Boolean = true,
    var isTwitch: Boolean = true,
    var isVimeo: Boolean = true,
    var isNico: Boolean = false,
    var isHttp: Boolean = true,
    var isLocal: Boolean = false,
)
