package lavalink.server.metrics;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import io.prometheus.client.Collector;
import io.prometheus.client.Histogram;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;
import static com.sun.management.GarbageCollectionNotificationInfo.from;

/**
 * Created by napster on 21.05.18.
 * <p>
 * General idea taken from {@link com.sedmelluq.discord.lavaplayer.tools.GarbageCollectionMonitor}, thanks!
 */
public class GcNotificationListener implements NotificationListener {

    private final Histogram gcPauses = Histogram.build()
            .name("lavalink_gc_pauses_seconds")
            .help("Garbage collection pauses by buckets")
            .buckets(0.025, 0.050, 0.100, 0.200, 0.400, 0.800, 1.600)
            .register();

    @Override
    public void handleNotification(Notification notification, Object handback) {
        if (GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType())) {
            GarbageCollectionNotificationInfo notificationInfo = from((CompositeData) notification.getUserData());
            GcInfo info = notificationInfo.getGcInfo();

            if (info != null && !"No GC".equals(notificationInfo.getGcCause())) {
                gcPauses.observe(info.getDuration() / Collector.MILLISECONDS_PER_SECOND);
            }
        }
    }
}
