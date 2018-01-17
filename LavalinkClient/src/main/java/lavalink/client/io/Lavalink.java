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

package lavalink.client.io;

import lavalink.client.LavalinkUtil;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.handle.SocketHandler;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Lavalink extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Lavalink.class);

    private boolean autoReconnect = true;
    private final int numShards;
    private final Function<Integer, JDA> jdaProvider;
    private final ConcurrentHashMap<String, Link> links = new ConcurrentHashMap<>();
    private final String userId;
    final List<LavalinkSocket> nodes = new CopyOnWriteArrayList<>();
    final LavalinkLoadBalancer loadBalancer = new LavalinkLoadBalancer(this);

    private final ScheduledExecutorService reconnectService;

    public Lavalink(String userId, int numShards, Function<Integer, JDA> jdaProvider) {
        this.userId = userId;
        this.numShards = numShards;
        this.jdaProvider = jdaProvider;

        reconnectService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "lavalink-reconnect-thread");
            thread.setDaemon(true);
            return thread;
        });
        reconnectService.scheduleWithFixedDelay(new ReconnectTask(this), 0, 500, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unused")
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    @SuppressWarnings("unused")
    public boolean getAutoReconnect() {
        return autoReconnect;
    }

    public void addNode(URI serverUri, String password) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", password);
        headers.put("Num-Shards", Integer.toString(numShards));
        headers.put("User-Id", userId);

        nodes.add(new LavalinkSocket(this, serverUri, new Draft_6455(), headers));
    }

    @SuppressWarnings("unused")
    public void removeNode(int key) {
        LavalinkSocket node = nodes.remove(key);
        node.close();
    }

    @SuppressWarnings("unused")
    @Nonnull
    public LavalinkLoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public Link getLink(String guildId) {
        return links.computeIfAbsent(guildId, __ -> new Link(this, guildId));
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public Link getLink(Guild guild) {
        return getLink(guild.getId());
    }

    @SuppressWarnings("WeakerAccess")
    public int getNumShards() {
        return numShards;
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public Collection<Link> getLinks() {
        return links.values();
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public List<LavalinkSocket> getNodes() {
        return nodes;
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

    public void shutdown() {
        reconnectService.shutdown();
        nodes.forEach(ReusableWebSocket::close);
    }

    void removeDestroyedLink(Link link) {
        log.info("Destroyed link for guild " + link.getGuildId());
        links.remove(link.getGuildId());
    }

    /*
     *  JDA event handling
     */

    @Override
    public void onReady(ReadyEvent event) {
        Map<String, SocketHandler> handlers = ((JDAImpl) event.getJDA()).getClient().getHandlers();
        handlers.put("VOICE_SERVER_UPDATE", new VoiceServerUpdateInterceptor(this, (JDAImpl) event.getJDA()));
        handlers.put("VOICE_STATE_UPDATE", new VoiceStateUpdateInterceptor(this, (JDAImpl) event.getJDA()));
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        Link link = links.get(event.getGuild().getId());
        if (link == null) return;

        ((JDAImpl) event.getJDA()).getClient().removeAudioConnection(link.getGuildIdLong());
        link.destroy();
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        Link link = links.get(event.getGuild().getId());
        if (link == null || !event.getChannel().equals(link.getChannel())) return;

        link.disconnect();
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        reconnectVoiceConnections(event.getJDA());
    }

    /* Util */

    private void reconnectVoiceConnections(JDA jda) {
        if (autoReconnect) {
            links.forEach((guildId, link) -> {
                try {
                    //Note: We also ensure that the link belongs to the JDA object
                    if (link.getChannel() != null
                            && jda.getGuildById(guildId) != null) {
                        link.connect(link.getChannel());
                    }
                } catch (Exception e) {
                    log.error("Caught exception while trying to reconnect link " + link, e);
                }
            });
        }
    }

    private void disconnectVoiceConnections(JDA jda) {
        links.forEach((guildId, link) -> {
            try {
                if (jda.getGuildById(guildId) != null) {
                    link.disconnect();
                }
            } catch (Exception e) {
                log.error("Caught exception while trying to disconnect link " + link, e);
            }
        });
    }

}
