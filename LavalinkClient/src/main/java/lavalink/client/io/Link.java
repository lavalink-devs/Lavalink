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

import lavalink.client.io.jda.VoiceStateUpdateInterceptor;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.WebSocketClient;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates which node we are linked to, what voice channel to use, and what player we are using
 */
abstract public class Link {

    private static final Logger log = LoggerFactory.getLogger(Link.class);
    private JSONObject lastVoiceServerUpdate = null;
    private final Lavalink lavalink;
    protected final long guild;
    private LavalinkPlayer player;
    private volatile String channel = null;
    private volatile LavalinkSocket node = null;
    /* May only be set by setState() */
    private volatile State state = State.NOT_CONNECTED;

    protected Link(Lavalink lavalink, String guildId) {
        this.lavalink = lavalink;
        this.guild = Long.parseLong(guildId);
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

    @SuppressWarnings("unused")
    public void resetPlayer() {
        player = null;
    }

    public String getGuildId() {
        return Long.toString(guild);
    }

    public long getGuildIdLong() {
        return guild;
    }

    public void disconnect() {
        setState(State.DISCONNECTING);
        queueAudioDisconnect();
    }

    public void changeNode(LavalinkSocket newNode) {
        node = newNode;
        if (lastVoiceServerUpdate != null) {
            node.send(lastVoiceServerUpdate.toString());
            player.onNodeChange();
        }
    }

    /**
     * Invoked when we get a voice state update telling us that we have disconnected.
     */
    public void onDisconnected() {
        setState(State.NOT_CONNECTED);
        LavalinkSocket socket = getNode(false);
        if (socket != null && state != State.DESTROYING && state != State.DESTROYED) {
            socket.send(new JSONObject()
                    .put("op", "destroy")
                    .put("guildId", Long.toString(guild))
                    .toString());
            node = null;
        }
    }

    /**
     * Disconnects the voice connection (if any) and internally dereferences this {@link Link}.
     * <p>
     * You should invoke this method your bot leaves a {@link net.dv8tion.jda.core.entities.Guild}.
     */
    @SuppressWarnings("unused")
    public void destroy() {
        boolean shouldDisconnect = state != State.DISCONNECTING && state != State.NOT_CONNECTED;
        setState(State.DESTROYING);
        if (shouldDisconnect) {
            queueAudioDisconnect();
        }
        setState(State.DESTROYED);
        lavalink.removeDestroyedLink(this);
        LavalinkSocket socket = getNode(false);
        if (socket != null) {
            socket.send(new JSONObject()
                    .put("op", "destroy")
                    .put("guildId", Long.toString(guild))
                    .toString());
        }
    }

    protected abstract void removeConnection();
    protected abstract void queueAudioDisconnect();
    protected abstract void queueAudioConnect(long channelId);

    /**
     * @return The current node
     */
    @Nullable
    @SuppressWarnings({"WeakerAccess", "unused"})
    public LavalinkSocket getNode() {
        return getNode(false);
    }

    /**
     * @param selectIfAbsent If true determines a new socket if there isn't one yet
     * @return The current node
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public LavalinkSocket getNode(boolean selectIfAbsent) {
        if (selectIfAbsent && node == null) {
            node = lavalink.loadBalancer.determineBestSocket(guild);
            if (player != null) player.onNodeChange();
        }
        return node;
    }

    /**
     * @return The channel we are currently connect to
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    @Nullable
    public String getChannel() {
        if (channel == null || state == State.DESTROYED || state == State.NOT_CONNECTED) return null;

        return channel;
    }

    /**
     * @return The channel we are currently connected to, or which we were connected to
     */
    @Nullable
    public String getLastChannel() {
        return channel;
    }

    /**
     * @return The {@link State} of this {@link Link}
     */
    @SuppressWarnings("unused")
    public State getState() {
        return state;
    }

    public void setState(@Nonnull State state) {
        if (this.state == State.DESTROYED && state != State.DESTROYED)
            throw new IllegalStateException("Cannot change state to " + state + " when state is " + State.DESTROYED);
        if (this.state == State.DESTROYING && state != State.DESTROYED) {
            throw new IllegalStateException("Cannot change state to " + state + " when state is " + State.DESTROYING);
        }
        log.debug("Link {} changed state from {} to {}", this, this.state, state);
        this.state = state;
    }

    /**
     * Invoked when we receive a voice state update from Discord, which tells us we have joined a channel
     */
    public void setChannel(@Nonnull VoiceChannel channel) {
        this.channel = channel.getId();
    }

    @Override
    public String toString() {
        return "Link{" +
                "guild='" + guild + '\'' +
                ", channel='" + channel + '\'' +
                ", state=" + state +
                '}';
    }

    public void onVoiceServerUpdate(JSONObject json) {
        lastVoiceServerUpdate = json;

        // Send WS message
        JSONObject out = new JSONObject();
        out.put("op", "voiceUpdate");
        out.put("sessionId", getSessionId());
        out.put("guildId", Long.toString(guild));
        out.put("event", lastVoiceServerUpdate);

        //noinspection ConstantConditions
        getNode(true).send(out.toString());
        setState(Link.State.CONNECTED);
    }

    /**
     * @return the session ID of this member in this Link's guild
     */
    protected abstract String getSessionId();

    public JSONObject getLastVoiceServerUpdate() {
        return lastVoiceServerUpdate;
    }

    public enum State {
        /**
         * Default, means we are not trying to use voice at all
         */
        NOT_CONNECTED,

        /**
         * Waiting for VOICE_SERVER_UPDATE
         */
        CONNECTING,

        /**
         * We have dispatched the voice server info to the server, and it should (soon) be connected.
         */
        CONNECTED,

        /**
         * Waiting for confirmation from Discord that we have connected
         */
        DISCONNECTING,

        /**
         * This {@link Link} is being destroyed
         */
        DESTROYING,

        /**
         * This {@link Link} has been destroyed and will soon (if not already) be unmapped from {@link Lavalink}
         */
        DESTROYED;
    }

}
