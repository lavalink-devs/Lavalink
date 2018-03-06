package lavalink.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by napster on 05.03.18.
 */
@Component
public class AudioSendFactoryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AudioSendFactoryConfiguration.class);

    private boolean nasSupported = false;
    private final int audioSendFactoryCount = Runtime.getRuntime().availableProcessors() * 2;

    public AudioSendFactoryConfiguration(ServerConfig serverConfig) {
        String os = System.getProperty("os.name");

        log.info("OS: " + System.getProperty("os.name") + ", Arch: " + System.getProperty("os.arch"));

        if ((os.contains("Windows") || os.contains("Linux"))
                && !System.getProperty("os.arch").equalsIgnoreCase("arm")
                && !System.getProperty("os.arch").equalsIgnoreCase("arm-linux")
                ) {
            nasSupported = true;
            log.info("JDA-NAS supported system detected. Enabled native audio sending.");

            Integer customBuffer = serverConfig.getBufferDurationMs();
            if (customBuffer != null) {
                log.info("Setting buffer to {}ms", customBuffer);
            } else {
                log.info("Using default buffer");
            }

            Integer customPlaylistLimit = serverConfig.getYoutubePlaylistLoadLimit();
            if (customPlaylistLimit != null) {
                log.info("Setting playlist load limit to {}", customPlaylistLimit);
            } else {
                log.info("Using default playlist load limit");
            }
        } else {
            log.warn("This system and architecture appears to not support native audio sending! "
                    + "GC pauses may cause your bot to stutter during playback.");
        }
    }

    public boolean isNasSupported() {
        return nasSupported;
    }

    public int getAudioSendFactoryCount() {
        return audioSendFactoryCount;
    }
}
