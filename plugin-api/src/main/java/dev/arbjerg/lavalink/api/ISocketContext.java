package dev.arbjerg.lavalink.api;

import org.json.JSONObject;

public interface ISocketContext {
    IPlayer getPlayer(long guildId);
    void destroyPlayer(long guildId);
    void sendMessage(JSONObject message);
    void closeWebsocket();
}
