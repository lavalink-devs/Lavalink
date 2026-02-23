package lavalink.server.config

import com.sedmelluq.lava.common.natives.architecture.DefaultArchitectureTypes
import com.sedmelluq.lava.common.natives.architecture.DefaultOperatingSystemTypes
import com.sedmelluq.lava.common.natives.architecture.SystemType
import moe.kyokobot.koe.KoeOptions
import moe.kyokobot.koe.poller.udpqueue.QueueManagerPool
import moe.kyokobot.koe.poller.udpqueue.UdpQueueFramePollerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KoeConfiguration(val serverConfig: ServerConfig) {

    private val log: Logger = LoggerFactory.getLogger(KoeConfiguration::class.java)
    private val supportedSystems = listOf(
        SystemType(DefaultArchitectureTypes.ARM, DefaultOperatingSystemTypes.LINUX),
        SystemType(DefaultArchitectureTypes.X86_64, DefaultOperatingSystemTypes.LINUX),
        SystemType(DefaultArchitectureTypes.X86_32, DefaultOperatingSystemTypes.LINUX),
        SystemType(DefaultArchitectureTypes.ARMv8_64, DefaultOperatingSystemTypes.LINUX),

        SystemType(DefaultArchitectureTypes.X86_64, DefaultOperatingSystemTypes.LINUX_MUSL),
        SystemType(DefaultArchitectureTypes.ARMv8_64, DefaultOperatingSystemTypes.LINUX_MUSL),

        SystemType(DefaultArchitectureTypes.X86_64, DefaultOperatingSystemTypes.WINDOWS),
        SystemType(DefaultArchitectureTypes.X86_32, DefaultOperatingSystemTypes.WINDOWS),

        SystemType(DefaultArchitectureTypes.X86_64, DefaultOperatingSystemTypes.DARWIN),
        SystemType(DefaultArchitectureTypes.ARMv8_64, DefaultOperatingSystemTypes.DARWIN)
    )

    @Bean
    fun koeOptions(): KoeOptions = KoeOptions.builder().apply {
        setDeafened(true)
        setEnableWSSPortOverride(false)

        val systemType: SystemType? = try {
            SystemType(DefaultArchitectureTypes.detect(), DefaultOperatingSystemTypes.detect())
        } catch (e: IllegalArgumentException) {
            null
        }
        log.info("OS: ${systemType?.osType ?: "unknown"}, Arch: ${systemType?.architectureType ?: "unknown"}")

        var bufferSize = serverConfig.bufferDurationMs ?: QueueManagerPool.DEFAULT_BUFFER_DURATION
        if (bufferSize <= 0) {
            log.info("JDA-NAS is disabled! GC pauses may cause your bot to stutter during playback.")
            return@apply
        }

        val nasSupported = supportedSystems.any { it.osType == systemType?.osType && it.architectureType == systemType.architectureType }

        if (nasSupported) {
            log.info("Enabling JDA-NAS")
            if (bufferSize < 40) {
                log.warn("Buffer size of ${bufferSize}ms is illegal. Defaulting to ${QueueManagerPool.DEFAULT_BUFFER_DURATION}ms")
                bufferSize = QueueManagerPool.DEFAULT_BUFFER_DURATION
            }
            try {
                setFramePollerFactory(
                    UdpQueueFramePollerFactory(
                        QueueManagerPool(
                            Runtime.getRuntime().availableProcessors(),
                            bufferSize
                        )
                    )
                )
            } catch (e: Throwable) {
                log.warn("Failed to enable JDA-NAS! GC pauses may cause your bot to stutter during playback.", e)
            }
        } else {
            log.warn(
                "This system and architecture appears to not support native audio sending! "
                        + "GC pauses may cause your bot to stutter during playback."
            )
        }
    }.create()
}
