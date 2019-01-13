package lavalink.server.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.logback.SentryAppender;
import lavalink.server.Launcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by napster on 25.04.18.
 */

@Configuration
public class SentryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SentryConfiguration.class);
    private static final String SENTRY_APPENDER_NAME = "SENTRY";

    public SentryConfiguration(ServerConfig serverConfig, SentryConfigProperties sentryConfig) {

        String dsn = sentryConfig.getDsn();
        boolean warnDeprecatedDsnConfig = false;
        if (dsn == null || dsn.isEmpty()) {
            //try deprecated config location
            dsn = serverConfig.getSentryDsn();
            warnDeprecatedDsnConfig = true;
        }

        if (dsn != null && !dsn.isEmpty()) {
            turnOn(dsn, sentryConfig.getTags());
            if (warnDeprecatedDsnConfig) {
                log.warn("Please update the location of the sentry dsn in lavalinks config file / your environment "
                        + "vars, it is now located under 'sentry.dsn' instead of 'lavalink.server.sentryDsn'.");
            }
        } else {
            turnOff();
        }
    }


    public void turnOn(String dsn, Map<String, String> tags) {
        log.info("Turning on sentry");
        SentryClient sentryClient = Sentry.init(dsn);

        if (tags != null) {
            tags.forEach(sentryClient::addTag);
        }

        // set the git commit hash this was build on as the release
        Properties gitProps = new Properties();
        try {
            //noinspection ConstantConditions
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

        getSentryLogbackAppender().start();
    }

    public void turnOff() {
        log.warn("Turning off sentry");
        Sentry.close();
        getSentryLogbackAppender().stop();
    }

    //programmatically creates a sentry appender
    private static synchronized SentryAppender getSentryLogbackAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);

        SentryAppender sentryAppender = (SentryAppender) root.getAppender(SENTRY_APPENDER_NAME);
        if (sentryAppender == null) {
            sentryAppender = new SentryAppender();
            sentryAppender.setName(SENTRY_APPENDER_NAME);

            ThresholdFilter warningsOrAboveFilter = new ThresholdFilter();
            warningsOrAboveFilter.setLevel(Level.WARN.levelStr);
            warningsOrAboveFilter.start();
            sentryAppender.addFilter(warningsOrAboveFilter);

            sentryAppender.setContext(loggerContext);
            root.addAppender(sentryAppender);
        }
        return sentryAppender;
    }

}
