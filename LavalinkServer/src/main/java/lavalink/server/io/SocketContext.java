package lavalink.server.io;

import lavalink.server.Launcher;
import net.dv8tion.jda.Core;
import org.java_websocket.WebSocket;

import java.util.HashMap;

public class SocketContext {

    private final WebSocket socket;
    private int shardCount;
    private final HashMap<Integer, Core> cores = new HashMap<>();

    SocketContext(WebSocket socket, int shardCount) {
        this.socket = socket;
        this.shardCount = shardCount;
    }

    Core getCore(int shardId) {
        return cores.computeIfAbsent(shardId,
                __ -> new Core(Launcher.config.getUserId(), new CoreClientImpl(socket, shardId))
        );
    }

    public int getShardCount() {
        return shardCount;
    }
}
