package demo;

import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import lavalink.server.plugin.LavalinkPlugin;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

@LavalinkPlugin.AutoRegister
public class Plugin implements LavalinkPlugin {
    @Override
    public void onStart(SocketServer server) {
        System.out.println("Hello World!");

        server.registerHandler("my-custom-op", Plugin::handleMyCustomOp);
    }

    @Override
    public void onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) {
        System.out.println("Received handshake");
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Connection opened");
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason) {
        System.out.println("Connection closed");
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
