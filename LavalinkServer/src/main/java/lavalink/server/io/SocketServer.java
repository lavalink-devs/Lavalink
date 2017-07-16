package lavalink.server.io;

import lavalink.server.util.Util;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static lavalink.server.io.WSCodes.*;

public class SocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final Map<WebSocket, SocketContext> contextMap = new HashMap<>();
    private final String password;

    public SocketServer(String password) {
        this.password = password;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            int shardCount = Integer.parseInt(clientHandshake.getFieldValue("Num-Shards"));

            if (clientHandshake.getFieldValue("Authorization").equals(password)) {
                log.info("Connection opened from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                contextMap.put(webSocket, new SocketContext(webSocket, shardCount));
            } else {
                log.error("Authentication failed from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                webSocket.close(AUTHORIZATION_REJECTED, "Authorization rejected");
            }
        } catch (Exception e) {
            log.error("Error when opening websocket", e);
            webSocket.close(INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        log.info("Connection closed from " + webSocket.getRemoteSocketAddress().toString() + " with protocol " + webSocket.getDraft());
        contextMap.remove(webSocket);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JSONObject json = new JSONObject(s);

        log.info(s);

        if (webSocket.isClosing()) {
            log.error("Ignoring closing websocket: " + webSocket.getRemoteSocketAddress().toString());
        }

        switch (json.getString("op")) {
            case "connect":
                contextMap.get(webSocket).getCore(getShardId(webSocket, json))
                        .getAudioManager(json.getString("guildId"))
                        .openAudioConnection(json.getString("channelId"));
                break;
            case "voiceUpdate":
                contextMap.get(webSocket).getCore(getShardId(webSocket, json)).provideVoiceServerUpdate(
                        json.getString("sessionId"),
                        json.getJSONObject("event")
                );
                break;
            case "disconnect":
                contextMap.get(webSocket).getCore(getShardId(webSocket, json)).getAudioManager(json.getString("guildId"))
                        .closeAudioConnection();
                break;
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log.error("Caught exception in websocket", e);
    }

    @Override
    public void onStart() {
        log.info("Started WS server");
    }

    //Shorthand method
    private int getShardId(WebSocket webSocket, JSONObject json) {
        return Util.getShardFromSnowflake(json.getString("guildId"), contextMap.get(webSocket).getShardCount());
    }

}
