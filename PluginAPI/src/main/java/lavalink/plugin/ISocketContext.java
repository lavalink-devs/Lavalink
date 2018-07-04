package lavalink.plugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.Core;
import org.java_websocket.WebSocket;

import java.util.List;
import java.util.Map;

public interface ISocketContext {
    Core getCore(int shardId);
    IPlayer getPlayer(String guildId);
    int getShardCount();
    WebSocket getSocket();
    Map<String, ? extends IPlayer> getPlayers();
    List<? extends IPlayer> getPlayingPlayers();
    void shutdown();
    AudioPlayerManager getAudioPlayerManager();
}
