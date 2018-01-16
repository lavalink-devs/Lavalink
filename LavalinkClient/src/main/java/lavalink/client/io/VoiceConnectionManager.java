package lavalink.client.io;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

class VoiceConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(VoiceConnectionManager.class);
    private static final Method JDA_SEND_METHOD;

    private final Lavalink lavalink;
    private final ConcurrentHashMap<Long, Link> pendingLinks = new ConcurrentHashMap<>();

    static {
        try {
            JDA_SEND_METHOD = WebSocketClient.class.getDeclaredMethod("send", String.class, boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        JDA_SEND_METHOD.setAccessible(true);
    }

    VoiceConnectionManager(Lavalink lavalink) {
        this.lavalink = lavalink;
        new VoiceConnectionQueueController().start();
    }

    void requestVoiceConnection(Link link) {
        pendingLinks.put(link.getGuildIdLong(), link);

        sendOp4((JDAImpl) link.getJda(),
                link.getGuildIdLong(),
                link.getChannel().getIdLong());
    }

    void disconnectVoiceConnection(Link link) {
        pendingLinks.remove(Long.parseLong(link.getGuildId()));
        
        sendOp4((JDAImpl) link.getJda(),
                link.getGuildIdLong(),
                null); // Null = disconnect
    }

    /**
     * Invoked by {@link VoiceServerUpdateInterceptor}
     *
     * @return true if the VOICE_SERVER_UPDATE is for a channel we expect to join
     */
    boolean onServerUpdate(long guildId, long expectedChannel) {
        /*
         This lambda atomically checks the pendingLinks map for the Link, and if it is in the expected channel.
         If it *is* for the expected channel, this lambda will return null, and otherwise the link.
         As a side effect, this removes the Link if we proceed.
         */
        Link result = pendingLinks.computeIfPresent(guildId, (__, link) -> {
            if (link.getChannel() != null
                    && link.getChannel().getIdLong() == expectedChannel) {
                return null;
            } else {
                return link;
            }
        });

        return result == null;
    }

    private void sendOp4(@Nonnull JDAImpl jda,
                         long guildId,
                         @Nullable Long channel) {

        String message = new JSONObject()
                .put("op", 4)
                .put("d", new JSONObject()
                        .put("guild_id", guildId)
                        .put("channel_id", channel == null ? JSONObject.NULL : channel))
                .toString();

        try {
            JDA_SEND_METHOD.invoke(jda.getClient(), message, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private class VoiceConnectionQueueController extends Thread {

        private VoiceConnectionQueueController() {
            setName("VoiceConnectionQueueController");
            setDaemon(true);
            setUncaughtExceptionHandler(
                    (__, e) -> log.error("Caught exception in VoiceConnectionQueueController. NOT GOOD", e)
            );
        }

        @Override
        public void run() {
            //TODO
        }
    }
}
