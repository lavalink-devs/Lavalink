package lavalink.server.config

import com.sedmelluq.lava.common.natives.architecture.DefaultArchitectureTypes
import com.sedmelluq.lava.common.natives.architecture.DefaultOperatingSystemTypes
import com.sedmelluq.lava.common.natives.architecture.SystemType
import moe.kyokobot.koe.KoeOptions
import moe.kyokobot.koe.codec.udpqueue.UdpQueueFramePollerFactory
import moe.kyokobot.koe.gateway.GatewayVersion
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
        setGatewayVersion(GatewayVersion.V8)
        setDeafened(true)
        setEnableWSSPortOverride(false)

        val systemType: SystemType? = try {
            SystemType(DefaultArchitectureTypes.detect(), DefaultOperatingSystemTypes.detect())
        } catch (e: IllegalArgumentException) {
            null
        }
        log.info("OS: ${systemType?.osType ?: "unknown"}, Arch: ${systemType?.architectureType ?: "unknown"}")

        var bufferSize = serverConfig.bufferDurationMs ?: UdpQueueFramePollerFactory.DEFAULT_BUFFER_DURATION
        if (bufferSize <= 0) {
            log.info("JDA-NAS is disabled! GC pauses may cause your bot to stutter during playback.")
            return@apply
        }

        val nasSupported = supportedSystems.any { it.osType == systemType?.osType && it.architectureType == systemType?.architectureType }

        if (nasSupported) {
            log.info("Enabling JDA-NAS")
            if (bufferSize < 40) {
                log.warn("Buffer size of ${bufferSize}ms is illegal. Defaulting to ${UdpQueueFramePollerFactory.DEFAULT_BUFFER_DURATION}ms")
                bufferSize = UdpQueueFramePollerFactory.DEFAULT_BUFFER_DURATION
            }
            try {
                setFramePollerFactory(
                    UdpQueueFramePollerFactory(
                        bufferSize,
                        Runtime.getRuntime().availableProcessors()
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
