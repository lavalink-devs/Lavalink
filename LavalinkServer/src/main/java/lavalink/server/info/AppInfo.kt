package lavalink.server.info

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*

/**
 * Created by napster on 25.06.18.
 *
 * Requires app.properties to be populated with values during the gradle build
 */
@Component
class AppInfo {
    companion object {
        private val log = LoggerFactory.getLogger(AppInfo::class.java)
    }

    final val versionBuild: String
    final val groupId: String
    final val artifactId: String
    final val buildTime: Long

    init {
        val resourceAsStream = this.javaClass.getResourceAsStream("/app.properties")
        val prop = Properties()
        try {
            prop.load(resourceAsStream)
        } catch (e: IOException) {
            log.error("Failed to load app.properties", e)
        }

        versionBuild = prop.getProperty("version")
        groupId = prop.getProperty("groupId")
        artifactId = prop.getProperty("artifactId")
        buildTime = try {
            prop.getProperty("buildTime").toLong()
        } catch (ignored: NumberFormatException) {
            -1
        }
    }
}
