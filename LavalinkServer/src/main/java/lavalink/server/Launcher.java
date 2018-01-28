/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server;

import ch.qos.logback.classic.LoggerContext;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.logback.SentryAppender;
import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import lavalink.server.util.SimpleLogToSLF4JAdapter;
import net.dv8tion.jda.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Controller
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public final static long startTime = System.currentTimeMillis();
    public static Config config;
    @SuppressWarnings("FieldCanBeLocal")
    private final SocketServer socketServer;

    @Autowired
    public Launcher(Config config, SocketServer socketServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered");
            try {
                socketServer.stop(30);
            } catch (InterruptedException e) {
                log.warn("Interrupted while stopping socket server", e);
            }
        }, "shutdown hook"));

        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        SimpleLog.addListener(new SimpleLogToSLF4JAdapter());
        Launcher.config = config;
        initSentry();
        this.socketServer = socketServer;
    }

    private void initSentry() {
        String sentryDsn = config.getSentryDsn();
        if (sentryDsn == null || sentryDsn.isEmpty()) {
            log.info("No sentry dsn found, turning off sentry.");
            turnOffSentry();
            return;
        }
        SentryClient sentryClient = Sentry.init(sentryDsn);
        log.info("Set up sentry.");

        // set the git commit hash this was build on as the release
        Properties gitProps = new Properties();
        try {
            gitProps.load(Launcher.class.getClassLoader().getResourceAsStream("git.properties"));
        } catch (NullPointerException | IOException e) {
            log.error("Failed to load git repo information", e);
        }

        String commitHash = gitProps.getProperty("git.commit.id");
        if (commitHash != null && !commitHash.isEmpty()) {
            log.info("Setting sentry release to commit hash {}", commitHash);
            sentryClient.setRelease(commitHash);
        } else {
            log.warn("No git commit hash found to set up sentry release");
        }
    }

    private void turnOffSentry() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        SentryAppender sentryAppender = (SentryAppender) lc.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("SENTRY");
        Sentry.close();
        sentryAppender.stop();
    }

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication(Launcher.class);
        sa.setWebEnvironment(true);
        sa.run(args);

        String os = System.getProperty("os.name");

        log.info("OS: " + System.getProperty("os.name") + ", Arch: " + System.getProperty("os.arch"));

        if ((os.contains("Windows") || os.contains("Linux"))
                && !System.getProperty("os.arch").equalsIgnoreCase("arm")
                && !System.getProperty("os.arch").equalsIgnoreCase("arm-linux")
                ) {
            SocketContext.nasSupported = true;
            log.info("JDA-NAS supported system detected. Enabled native audio sending.");

            Integer customBuffer = config.getBufferDurationMs();
            if (customBuffer != null) {
                log.info("Setting buffer to {}ms", customBuffer);
            } else {
                log.info("Using default buffer");
            }
        } else {
            log.warn("This system and architecture appears to not support native audio sending! "
                    + "GC pauses may cause your bot to stutter during playback.");
        }
    }

    @Bean
    static SocketServer socketServer(@Value("${lavalink.server.ws.port}") Integer port,
                                     @Value("${lavalink.server.ws.host}") String host,
                                     @Value("${lavalink.server.password}") String password) {
        SocketServer ss = new SocketServer(new InetSocketAddress(host, port), password);
        ss.start();
        return ss;
    }

}
