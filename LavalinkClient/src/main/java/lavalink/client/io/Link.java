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
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Indicates which node we are linked to, what voice channel to use, and what player we are using
 */
public class Link {

    private static final Logger log = LoggerFactory.getLogger(Link.class);
    private static final ScheduledExecutorService DISCONNECT_TIMEOUT_SERVICE = Executors.newSingleThreadScheduledExecutor();
    /**
     * Time before we forcefully reconnect if we don't receive our leave event
     */
    private static final int DISCONNECT_TIMEOUT_MS = 5000;

    private final Lavalink lavalink;
    private final String guild;
    private LavalinkPlayer player;
    /**
     * Channel we are currently connected to or disconnecting/connecting from/to, if any
     */
    private volatile String currentChannel = null;
    /**
     * Channel we will connect to after disconnecting from {@code currentChannel}
     */
    private volatile String pendingChannel = null;
    /**
     * The node we are currently connected to. Automatically assigned.
     * Can only be reassigned by reconnecting any existing voice connection.
     */
    private volatile LavalinkSocket currentNode = null;
    private volatile LavalinkSocket pendingNode = null;
    private volatile State state = State.NO_CHANNEL;

    private volatile Instant lastTimeDisconnected = Instant.EPOCH;

    Link(Lavalink lavalink, String guildId) {
        this.lavalink = lavalink;
        this.guild = guildId;
    }

    public LavalinkPlayer getPlayer() {
        if (player == null) {
            player = new LavalinkPlayer(this);
        }

        return player;
    }

    public Lavalink getLavalink() {
        return lavalink;
    }

    public String getGuildId() {
        return guild;
    }

    /**
     * Eventually connect to a channel. Takes care of disconnecting from an existing connection
     *
     * @param channel Channel to connect to
     */
    @SuppressWarnings("WeakerAccess")
    public void connect(VoiceChannel channel) {
        if (currentNode == null) {
            currentNode = lavalink.loadBalancer.determineBestSocket();
        }

        if (state == State.NO_CHANNEL) {
            connectNow(channel.getId());
            pendingChannel = null;
        } else {
            pendingChannel = channel.getId();

            if (state == State.CONNECTED) {
                // Make sure we eventually reconnect
                DISCONNECT_TIMEOUT_SERVICE.schedule(disconnectTimeoutHandler, DISCONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);

                disconnect();
            }
        }
    }

    /**
     * Invoked when we are ready too establish a connection
     *
     * @param channelId Channel to connect to
     */
    private void connectNow(String channelId) {
        VoiceChannel channel = getJda()
                .getVoiceChannelById(channelId);

        assert channel.getGuild().getId().equals(guild);

        currentChannel = channelId;
        pendingChannel = null;
        state = State.CONNECTING;

        JSONObject json = new JSONObject();
        json.put("op", "connect");
        json.put("guildId", channel.getGuild().getId());
        json.put("channelId", channel.getId());
        currentNode.send(json.toString());
    }

    private void disconnect(boolean reconnect) {
        if (state == State.NO_CHANNEL) {
            log.warn("Attempt to disconnect from channel when not connected. Guild: " + guild);
            return;
        }

        state = reconnect
                ? State.DISCONNECTING_BEFORE_RECONNECTING
                : State.DISCONNECTING;

        JSONObject json = new JSONObject();
        json.put("op", "disconnect");
        json.put("guildId", guild);
        currentNode.send(json.toString());
    }

    @SuppressWarnings("WeakerAccess")
    public void disconnect() {
        disconnect(false);
    }

    void changeNode(LavalinkSocket newNode) {
        if (state == State.NO_CHANNEL) {
            changeNode0(newNode);
        } else {
            // Since we are connected, we will need to disconnect before truly changing node
            pendingNode = newNode;
            disconnect(true);
        }
    }

    private void changeNode0(LavalinkSocket newNode) {
        currentNode = newNode;
        pendingNode = null;
        getPlayer().onNodeChange();
    }

    @SuppressWarnings("WeakerAccess")
    public LavalinkSocket getCurrentSocket() {
        return currentNode;
    }

    void onVoiceJoin() {
        state = State.CONNECTED;
    }

    void onVoiceLeave() {
        lastTimeDisconnected = Instant.now();

        if (pendingNode != null) {
            // Disconnecting means we can change to the pending node, if any
            changeNode0(pendingNode);
        }

        if (pendingChannel != null) {
            connectNow(pendingChannel);
            pendingChannel = null;
        } else if (state == State.DISCONNECTING_BEFORE_RECONNECTING) {
            connectNow(currentChannel);
        } else {
            state = State.NO_CHANNEL;
            currentChannel = null;
        }
    }

    void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        log.info("Moved from " + event.getChannelLeft() + " to " + event.getChannelJoined());

        if (!event.getChannelLeft().getId().equals(currentChannel)) {
            log.warn("Moved away from channel " + currentChannel + " but expected channel is " + currentChannel + "!");
        }

        currentChannel = event.getChannelJoined().getId();
    }

    void onNodeDisconnected() {
        changeNode(lavalink.loadBalancer.determineBestSocket());

        // This will trigger a leave
        ((JDAImpl) getJda()).getClient()
                .send("{\"op\":4,\"d\":{\"self_deaf\":false,\"guild_id\":\"" + guild + "\",\"channel_id\":null,\"self_mute\":false}}");
    }

    private final Runnable disconnectTimeoutHandler = () -> {
        if(lastTimeDisconnected.plusMillis(DISCONNECT_TIMEOUT_MS).isBefore(Instant.now())) {
            // We didn't get the leave event, so we *must* not be connected
            log.warn("Attempted to disconnect from voice to reconnect but timed out after " + DISCONNECT_TIMEOUT_MS
                    + "ms. Pretending that we left so we don't get stuck...");
            onVoiceLeave();
        }
    };

    /**
     * @return The channel we are currently connect to, even if we are trying to connect to a different one
     */
    @SuppressWarnings("WeakerAccess")
    public VoiceChannel getCurrentChannel() {
        if (currentChannel == null) return null;

        return getJda()
                .getVoiceChannelById(currentChannel);
    }

    public JDA getJda() {
        return lavalink.getJdaFromSnowflake(guild);
    }

    private enum State {
        /**
         * Default, means we are not trying to use voice at all
         */
        NO_CHANNEL,

        /**
         * Connecting to voice
         */
        CONNECTING,

        /**
         * Connected (or attempting to reconnect) to voice
         */
        CONNECTED,

        /**
         * We are trying to disconnect, after which we will switch to NO_CHANNEL
         */
        DISCONNECTING,

        /**
         * We are trying to disconnect, after which we will switch to CONNECTING. Used for changing node
         */
        DISCONNECTING_BEFORE_RECONNECTING
    }

}
