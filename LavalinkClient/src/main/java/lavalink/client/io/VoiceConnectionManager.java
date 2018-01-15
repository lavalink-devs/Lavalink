package lavalink.client.io;

import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceConnectionManager {

    private final Lavalink lavalink;
    private ConcurrentHashMap<Long, Link> pendingLinks = new ConcurrentHashMap<>();

    public VoiceConnectionManager(Lavalink lavalink) {
        this.lavalink = lavalink;
    }

    void requestVoiceConnection(Link link) {
        pendingLinks.put(link.getGuildIdLong(), link);

        sendOp4((JDAImpl) link.getJda(),
                link.getGuildIdLong(),
                link.getChannel().getIdLong());
    }

    void requestVoiceMove(Link link, long newChannel) {
        sendOp4((JDAImpl) link.getJda(),
                link.getGuildIdLong(),
                newChannel);
    }

    void disconnectVoiceConnection(Link link) {
        pendingLinks.remove(Long.parseLong(link.getGuildId()));
        sendOp4((JDAImpl) link.getJda(),
                link.getGuildIdLong(),
                null); // Null = disconnect
    }

    /**
     * Invoked by {@link VoiceServerUpdateInterceptor}
     * @return true if the Voice_SERVER_UPDATE is for a channel we expect to join
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

        jda.getClient().send(new JSONObject()
                .put("op", 4)
                .put("d", new JSONObject()
                        .put("guild_id", guildId)
                        .put("channel_id", channel == null ? JSONObject.NULL : channel))
                .toString());
    }
}
