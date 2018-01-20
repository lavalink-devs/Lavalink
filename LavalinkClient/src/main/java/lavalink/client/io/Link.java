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
public class Link {

    private static final Logger log = LoggerFactory.getLogger(Link.class);

    private final Lavalink lavalink;
    private final long guild;
    private LavalinkPlayer player;
    private volatile String channel = null;
    private volatile LavalinkSocket node = null;
    /* May only be set by setState() */
    private volatile State state = State.NOT_CONNECTED;
    private volatile boolean reconnectToNewNode = false;

    Link(Lavalink lavalink, String guildId) {
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

    /**
     * Eventually connect to a channel. Takes care of disconnecting from an existing connection
     *
     * @param channel Channel to connect to
     */
    @SuppressWarnings("WeakerAccess")
    public void connect(VoiceChannel channel) {
        if (!channel.getGuild().equals(getJda().getGuildById(guild)))
            throw new IllegalArgumentException("The provided VoiceChannel is not a part of the Guild that this AudioManager handles." +
                    "Please provide a VoiceChannel from the proper Guild");
        if (!channel.getGuild().isAvailable())
            throw new GuildUnavailableException("Cannot open an Audio Connection with an unavailable guild. " +
                    "Please wait until this Guild is available to open a connection.");
        final Member self = channel.getGuild().getSelfMember();
        if (!self.hasPermission(channel, Permission.VOICE_CONNECT) && !self.hasPermission(channel, Permission.VOICE_MOVE_OTHERS))
            throw new InsufficientPermissionException(Permission.VOICE_CONNECT);

        //If we are already connected to this VoiceChannel, then do nothing.
        if (channel.equals(channel.getGuild().getSelfMember().getVoiceState().getChannel()))
            return;

        final int userLimit = channel.getUserLimit(); // userLimit is 0 if no limit is set!
        if (!self.isOwner() && !self.hasPermission(Permission.ADMINISTRATOR)) {
            final long perms = PermissionUtil.getExplicitPermission(channel, self);
            final long voicePerm = Permission.VOICE_MOVE_OTHERS.getRawValue();
            if (userLimit > 0                                                   // If there is a userlimit
                    && userLimit <= channel.getMembers().size()                 // if that userlimit is reached
                    && (perms & voicePerm) != voicePerm)                        // If we don't have voice move others permissions
                throw new InsufficientPermissionException(Permission.VOICE_MOVE_OTHERS, // then throw exception!
                        "Unable to connect to VoiceChannel due to userlimit! Requires permission VOICE_MOVE_OTHERS to bypass");
        }

        setState(State.CONNECTING);
        getMainWs().queueAudioConnect(channel);
    }

    public void disconnect() {
        Guild g = getJda().getGuildById(guild);

        if (g == null) return;

        setState(State.DISCONNECTING);
        getMainWs().queueAudioDisconnect(g);
    }

    public void changeNode(LavalinkSocket newNode) {
        disconnect();
        node = newNode;
        connect(getJda().getVoiceChannelById(channel));
        reconnectToNewNode = true;
    }

    /**
     * Disconnects the voice connection (if any) and internally dereferences this {@link Link}.
     * <p>
     * You should invoke this method your bot leaves a {@link net.dv8tion.jda.core.entities.Guild}.
     */
    @SuppressWarnings("unused")
    public void destroy() {
        if (state != State.DISCONNECTING && state != State.NOT_CONNECTED) {
            disconnect();
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
        }
        return node;
    }

    /**
     * @return The channel we are currently connect to
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public VoiceChannel getChannel() {
        if (channel == null) return null;

        return getJda().getVoiceChannelById(channel);
    }

    /**
     * @return The {@link State} of this {@link Link}
     */
    @SuppressWarnings("unused")
    public State getState() {
        return state;
    }

    void setState(@Nonnull State state) {
        if (this.state == State.DESTROYED && state != State.DESTROYED)
            throw new IllegalStateException("Cannot change state to " + state + " when state is " + State.DESTROYED);

        log.debug("Link {} changed state from {} to {}", this, this.state, state);
        this.state = state;

        if (state == State.NOT_CONNECTED && reconnectToNewNode) {
            reconnectToNewNode = false;
            connect(getJda().getVoiceChannelById(channel));
        } else if (state == State.NOT_CONNECTED || state == State.DESTROYED) {
            channel = null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Nonnull
    public JDA getJda() {
        return lavalink.getJdaFromSnowflake(String.valueOf(guild));
    }

    private WebSocketClient getMainWs() {
        return ((JDAImpl) getJda()).getClient();
    }

    /**
     * Setter used by {@link VoiceStateUpdateInterceptor} to change the expected channel
     */
    void setChannel(@Nonnull VoiceChannel channel) {
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
         * This {@link Link} has been destroyed and will soon (if not already) be unmapped from {@link Lavalink}
         */
        DESTROYED
    }

}
