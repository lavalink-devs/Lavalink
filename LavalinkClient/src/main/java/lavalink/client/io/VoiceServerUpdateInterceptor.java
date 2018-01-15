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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceServerUpdateInterceptor extends SocketHandler {

    private static final Logger log = LoggerFactory.getLogger(VoiceServerUpdateInterceptor.class);

    private final Lavalink lavalink;

    VoiceServerUpdateInterceptor(Lavalink lavalink, JDAImpl jda) {
        super(jda);
        this.lavalink = lavalink;
    }

    @Override
    protected Long handleInternally(JSONObject content) {
        log.info(content.toString());
        long idLong = content.getLong("guild_id");

        if (api.getGuildLock().isLocked(idLong))
            return idLong;

        // Get session
        Guild guild = api.getGuildMap().get(idLong);
        if (guild == null)
            throw new IllegalArgumentException("Attempted to start audio connection with Guild that doesn't exist! JSON: " + content);
        String sessionId = guild.getSelfMember().getVoiceState().getSessionId();

        // Send WS message
        JSONObject json = new JSONObject();
        json.put("op", "voiceUpdate");
        json.put("sessionId", sessionId);
        json.put("guildId", guild.getId());
        json.put("event", content);
        //noinspection ConstantConditions
        lavalink.getLink(guild).getNode(true).send(json.toString());

        log.info("Sent voice update for guild {}", idLong);

        return null;
    }
}
