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
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.logback.SentryAppender;
import lavalink.server.config.ServerConfig;
import lavalink.server.info.AppInfo;
import lavalink.server.info.GitRepoState;
import lavalink.server.io.SocketServer;
import lavalink.server.util.SimpleLogToSLF4JAdapter;
import net.dv8tion.jda.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@SpringBootApplication
@ComponentScan
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public final static long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        if (args.length > 0
                && (args[0].equalsIgnoreCase("-v")
                    || args[0].equalsIgnoreCase("--version"))) {
            System.out.println("Version flag detected. Printing version info, then exiting.");
            System.out.println(getVersionInfo());
            return;
        }

        SpringApplication sa = new SpringApplication(Launcher.class);
        sa.setWebApplicationType(WebApplicationType.SERVLET);
        sa.addListeners(
                event -> {
                    if (event instanceof ApplicationEnvironmentPreparedEvent) {
                        log.info(getVersionInfo());
                    }
                },
                event -> {
                    if (event instanceof ApplicationFailedEvent) {
                        ApplicationFailedEvent failed = (ApplicationFailedEvent) event;
                        log.error("Application failed", failed.getException());
                    }
                }
        );
        sa.run(args);
    }

    public Launcher(ServerConfig serverConfig, SocketServer socketServer) {
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
        initSentry(serverConfig);
    }

    private void initSentry(ServerConfig serverConfig) {
        String sentryDsn = serverConfig.getSentryDsn();
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

    private static String getVersionInfo() {
        AppInfo appInfo = new AppInfo();
        GitRepoState gitRepoState = new GitRepoState();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")
                .withZone(ZoneId.of("Europe/Copenhagen"));
        String buildTime = dtf.format(Instant.ofEpochMilli(appInfo.getBuildTime()));
        String commitTime = dtf.format(Instant.ofEpochMilli(gitRepoState.getCommitTime() * 1000));

        return "\n\n" + getVanity()
                + "\n"
                + "\n\tVersion:        " + appInfo.getVersion()
                + "\n\tBuild:          " + appInfo.getBuildNumber()
                + "\n\tBuild time:     " + buildTime
                + "\n\tBranch          " + gitRepoState.getBranch()
                + "\n\tCommit:         " + gitRepoState.getCommitIdAbbrev()
                + "\n\tCommit time:    " + commitTime
                + "\n\tJVM:            " + System.getProperty("java.version")
                + "\n\tLavaplayer      " + PlayerLibrary.VERSION
                + "\n";
    }

    private static String getVanity() {
        //ansi color codes
        String red = "[31m";
        String green = "[32m";
        String defaultC = "[0m";

        String vanity
                = "g       .  r _                  _ _       _    g__ _ _\n"
                + "g      /\\\\ r| | __ ___   ____ _| (_)_ __ | | __g\\ \\ \\ \\\n"
                + "g     ( ( )r| |/ _` \\ \\ / / _` | | | '_ \\| |/ /g \\ \\ \\ \\\n"
                + "g      \\\\/ r| | (_| |\\ V / (_| | | | | | |   < g  ) ) ) )\n"
                + "g       '  r|_|\\__,_| \\_/ \\__,_|_|_|_| |_|_|\\_\\g / / / /\n"
                + "d    =========================================g/_/_/_/d";

        vanity = vanity.replaceAll("r", red);
        vanity = vanity.replaceAll("g", green);
        vanity = vanity.replaceAll("d", defaultC);
        return vanity;
    }
}
