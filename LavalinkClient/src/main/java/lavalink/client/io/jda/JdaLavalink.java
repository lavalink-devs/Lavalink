package lavalink.client.io.jda;

import lavalink.client.LavalinkUtil;
import lavalink.client.io.Lavalink;
import lavalink.client.io.Link;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

public class JdaLavalink extends Lavalink<JdaLink> implements EventListener {

    private static final Logger log = LoggerFactory.getLogger(JdaLavalink.class);
    private final Function<Integer, JDA> jdaProvider;
    private boolean autoReconnect = true;

    public JdaLavalink(String userId, int numShards, Function<Integer, JDA> jdaProvider) {
        super(userId, numShards);
        this.jdaProvider = jdaProvider;
    }

    @SuppressWarnings("unused")
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    @SuppressWarnings("unused")
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public JdaLink getLink(Guild guild) {
        return getLink(guild.getId());
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public JDA getJda(int shardId) {
        return jdaProvider.apply(shardId);
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public JDA getJdaFromSnowflake(String snowflake) {
        return jdaProvider.apply(LavalinkUtil.getShardFromSnowflake(snowflake, numShards));
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof ReadyEvent) {
            Map<String, SocketHandler> handlers = ((JDAImpl) event.getJDA()).getClient().getHandlers();
            handlers.put("VOICE_SERVER_UPDATE", new VoiceServerUpdateInterceptor(this, (JDAImpl) event.getJDA()));
            handlers.put("VOICE_STATE_UPDATE", new VoiceStateUpdateInterceptor(this, (JDAImpl) event.getJDA()));
        } else if (event instanceof ReconnectedEvent) {
            if (autoReconnect) {
                getLinksMap().forEach((guildId, link) -> {
                    try {
                        //Note: We also ensure that the link belongs to the JDA object
                        if (link.getLastChannel() != null
                                && event.getJDA().getGuildById(guildId) != null) {
                            link.connect(event.getJDA().getVoiceChannelById(link.getLastChannel()), false);
                        }
                    } catch (Exception e) {
                        log.error("Caught exception while trying to reconnect link " + link, e);
                    }
                });
            }
        } else if (event instanceof GuildLeaveEvent) {
            JdaLink link = getLinksMap().get(((GuildLeaveEvent) event).getGuild().getId());
            if (link == null) return;

            link.removeConnection();
        } else if (event instanceof VoiceChannelDeleteEvent) {
            VoiceChannelDeleteEvent e = (VoiceChannelDeleteEvent) event;
            JdaLink link = getLinksMap().get(e.getGuild().getId());
            if (link == null || !e.getChannel().getId().equals(link.getLastChannel())) return;

            link.removeConnection();
        }
    }

    @Override
    protected JdaLink buildNewLink(String guildId) {
        return new JdaLink(this, guildId);
    }
}
