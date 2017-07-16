package lavalink.server.io;

import lavalink.server.Launcher;
import net.dv8tion.jda.Core;
import org.java_websocket.WebSocket;

public class SocketContext {

    private final WebSocket socket;
    private final CoreClientImpl coreClient;
    private final Core core;

    SocketContext(WebSocket socket) {
        this.socket = socket;
        this.coreClient = new CoreClientImpl(socket);
        this.core = new Core(Launcher.config.getUserId(), coreClient);
    }

    public WebSocket getSocket() {
        return socket;
    }

    public CoreClientImpl getCoreClient() {
        return coreClient;
    }

    public Core getCore() {
        return core;
    }

}
