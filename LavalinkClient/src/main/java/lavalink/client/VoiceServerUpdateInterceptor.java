package lavalink.client;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.handle.SocketHandler;
import org.json.JSONObject;

public class VoiceServerUpdateInterceptor extends SocketHandler {

    private final Lavalink lavalink;

    VoiceServerUpdateInterceptor(Lavalink lavalink, JDAImpl jda) {
        super(jda);
        this.lavalink = lavalink;
    }

    @Override
    protected Long handleInternally(JSONObject content) {
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
        json.put("message", content);
        lavalink.getSocket().send(json.toString());
    }
}
