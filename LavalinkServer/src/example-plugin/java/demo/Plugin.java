package demo;

import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import lavalink.server.plugin.LavalinkPlugin;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

@LavalinkPlugin.AutoRegister
public class Plugin implements LavalinkPlugin {
    @Override
    public void onStart(SocketServer server) {
        System.out.println("Hello World!");

        server.registerHandler("my-custom-op", Plugin::handleMyCustomOp);
    }

    @Override
    public void onShutdown() {
        System.out.println("Goodbye World");
    }

    private static void handleMyCustomOp(SocketServer server, SocketContext context, WebSocket socket, JSONObject json) {
        System.out.println("Received custom op");
        socket.send(new JSONObject().put("one", 1).toString());
    }
}
