package lavalink.api;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.json.JSONObject;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketSession;

public interface ISocketContext {
    WebSocketSession getWebSocketSession();
    @Nullable
    IPlayer getPlayer(long guildId);
    void destroyPlayer(long guildId);
    void sendMessage(JSONObject message);
    void closeWebsocket();
}
