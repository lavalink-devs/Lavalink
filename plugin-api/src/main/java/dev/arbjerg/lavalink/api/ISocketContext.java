package dev.arbjerg.lavalink.api;

import org.json.JSONObject;

import java.util.Map;

public interface ISocketContext {
    IPlayer getPlayer(long guildId);
    Map<Long, IPlayer> getPlayers();
    void destroyPlayer(long guildId);
    void sendMessage(JSONObject message);
    void closeWebSocket();
    void closeWebSocket(int closeCode);
    void closeWebSocket(int closeCode, String reason);
}
