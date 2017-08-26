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

import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.java_websocket.drafts.Draft_6455;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Lavalink extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(Lavalink.class);

    private final int numShards;
    private final Function<Integer, JDA> jdaProvider;
    private final ConcurrentHashMap<String, String> connectedChannels = new ConcurrentHashMap<>(); // Key is guild id
    private final ConcurrentHashMap<String, LavalinkPlayer> players = new ConcurrentHashMap<>(); // Key is guild id
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

    public void openVoiceConnection(VoiceChannel channel) {
        LavalinkSocket socket = loadBalancer.getSocket(channel.getGuild());
        connectedChannels.put(channel.getGuild().getId(), channel.getId());

        if (socket == null || socket.isClosed()) return;

        JSONObject json = new JSONObject();
        json.put("op", "connect");
        json.put("guildId", channel.getGuild().getId());
        json.put("channelId", channel.getId());
        socket.send(json.toString());
    }

    public void closeVoiceConnection(Guild guild) {
        LavalinkSocket socket = loadBalancer.getSocket(guild);
        connectedChannels.remove(guild.getId());

        if (socket == null || socket.isClosed()) return;

        JSONObject json = new JSONObject();
        json.put("op", "disconnect");
        json.put("guildId", guild.getId());
        socket.send(json.toString());
    }

    public VoiceChannel getConnectedChannel(Guild guild) {
        String id = connectedChannels.getOrDefault(guild.getId(), null);
        if (id != null) {
            return guild.getVoiceChannelById(id);
        }
        return null;
    }

    public String getConnectedChannel(String guildId) {
        return connectedChannels.getOrDefault(guildId, null);
    }

    public LavalinkPlayer getPlayer(String guildId) {
        return players.computeIfAbsent(guildId, __ -> new LavalinkPlayer(this, loadBalancer.getSocket(guildId), guildId));
    }

    public void shutdown() {
        reconnectService.shutdown();
        nodes.forEach(ReusableWebSocket::close);
    }

    LavalinkSocket getSocket(String guildId) {
        return loadBalancer.getSocket(guildId);
    }

    public JDA getShard(int num) {
        return jdaProvider.apply(num);
    }

    public int getNumShards() {
        return numShards;
    }

    @SuppressWarnings("WeakerAccess")
    public List<LavalinkSocket> getNodes() {
        return nodes;
    }

    /* JDA event handling */

    @Override
    public void onReady(ReadyEvent event) {
        ((JDAImpl) event.getJDA()).getClient().getHandlers()
                .put("VOICE_SERVER_UPDATE", new VoiceServerUpdateInterceptor(this, (JDAImpl) event.getJDA()));
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        reconnectTheVoiceConnections(event.getJDA());
    }

    @Override
    public void onResume(ResumedEvent event) {
        reconnectTheVoiceConnections(event.getJDA());
    }

    private void reconnectTheVoiceConnections(JDA jda) {
        connectedChannels.forEach((guildId, channel) -> {
            try {
                Guild guild = jda.getGuildById(guildId);
                if (guild != null) {
                    openVoiceConnection(guild.getVoiceChannelById(channel));
                }
            } catch (Exception e) {
                int shardId = jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();
                log.error("Caught exception while trying to reconnect shard " + shardId, e);
            }
        });
    }
}
