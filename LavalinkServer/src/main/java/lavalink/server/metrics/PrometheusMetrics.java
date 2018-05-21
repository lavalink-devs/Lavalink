package lavalink.server.metrics;

import ch.qos.logback.classic.LoggerContext;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.logback.InstrumentedAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * Created by napster on 08.05.18.
 */
@Component
@ConditionalOnProperty("metrics.prometheus.enabled")
public class PrometheusMetrics {

    private static final Logger log = LoggerFactory.getLogger(PrometheusMetrics.class);

    public PrometheusMetrics() {

        InstrumentedAppender prometheusAppender = new InstrumentedAppender();
        //log metrics
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final ch.qos.logback.classic.Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        prometheusAppender.setContext(root.getLoggerContext());
        prometheusAppender.start();
        root.addAppender(prometheusAppender);

        //jvm (hotspot) metrics
        DefaultExports.initialize();

        //gc pause buckets
        final GcNotificationListener gcNotificationListener = new GcNotificationListener();
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (gcBean instanceof NotificationEmitter) {
                ((NotificationEmitter) gcBean).addNotificationListener(gcNotificationListener, null, gcBean);
            }
        }

        log.info("Prometheus metrics set up");
    }
}
