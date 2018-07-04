package lavalink.plugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public interface ISocketServer {
    int getShardId(WebSocket webSocket, JSONObject json);
    Collection<? extends ISocketContext> getContexts();
    Map<String, ? extends WebsocketOperationHandler> getHandlers();
    WebsocketOperationHandler getHandler(String op);
    boolean registerHandler(String op, WebsocketOperationHandler handler, boolean override);
    boolean registerHandler(String op, WebsocketOperationHandler handler);
    Supplier<AudioPlayerManager> getAudioPlayerManagerSupplier();
}
