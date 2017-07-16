package lavalink.client;

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
        lavalink.getSocket().send(json.toString());

        log.info("Sent voice update");

        return null;
    }
}
