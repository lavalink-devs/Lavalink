/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server.io;

import lavalink.server.player.Player;
import lavalink.server.util.Util;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static lavalink.server.io.WSCodes.AUTHORIZATION_REJECTED;
import static lavalink.server.io.WSCodes.INTERNAL_ERROR;

public class SocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);
    private static final Map<WebSocket, SocketContext> contextMap = new HashMap<>();
    private static final Map<String, SocketContext> contextIdentifierMap = new HashMap<>();
    private final String password;

    @Value("${server.port}")
    private String restPort;

    public SocketServer(InetSocketAddress address, String password) {
        super(address);
        this.password = password;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        try {
            int shardCount = Integer.parseInt(clientHandshake.getFieldValue("Num-Shards"));
            String userId = clientHandshake.getFieldValue("User-Id");

            if (clientHandshake.getFieldValue("Authorization").equals(password)) {
                log.info("Connection opened from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                String identifier = UUID.randomUUID().toString();
                SocketContext socketContext = new SocketContext(webSocket, userId, shardCount, identifier);
                contextMap.put(webSocket, socketContext);
                String host = clientHandshake.getFieldValue("Host");
                sendHello(webSocket, socketContext, identifier, restBaseUrl(host));
            } else {
                log.error("Authentication failed from " + webSocket.getRemoteSocketAddress() + " with protocol " + webSocket.getDraft());
                webSocket.close(AUTHORIZATION_REJECTED, "Authorization rejected");
            }
        } catch (Exception e) {
            log.error("Error when opening websocket", e);
            webSocket.close(INTERNAL_ERROR, e.getMessage());
        }
    }

    private void sendHello(WebSocket webSocket, SocketContext socketContext, String identifier, String restBaseUrl) {
        JSONObject hello = new JSONObject()
                .put("op", "hello")
                .put("identifier", identifier)
                .put("restBase", restBaseUrl);
        log.info(hello.toString());
        webSocket.send(hello.toString());
        contextIdentifierMap.put(identifier, socketContext);
    }

    private String restBaseUrl(String host) {
        return "http://" + host.replaceAll(":\\d+", "") + ":" + restPort + "/";
    }

    public static SocketContext getContext(String identifier) {
        return contextIdentifierMap.get(identifier);
    }

    @Override
    public void onCloseInitiated(WebSocket webSocket, int code, String reason) {
        close(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason, boolean remote) {
        close(webSocket, code, reason);
    }

    @Override
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        close(webSocket, code, reason);
    }

    // WebSocketServer has a very questionable attitude towards communicating close events, so we override ALL the closing methods
    private void close(WebSocket webSocket, int code, String reason) {
        SocketContext context = contextMap.remove(webSocket);
        if (context != null) {
            log.info("Connection closed from {} with protocol {} with reason {} with code {}",
                    webSocket.getRemoteSocketAddress().toString(), webSocket.getDraft(), reason, code);
            context.shutdown();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JSONObject json = new JSONObject(s);

        log.info(s);

        if (webSocket.isClosing()) {
            log.error("Ignoring closing websocket: " + webSocket.getRemoteSocketAddress().toString());
            return;
        }

        switch (json.getString("op")) {
            /* JDAA ops */
            case "voiceUpdate":
                contextMap.get(webSocket).getCore(getShardId(webSocket, json)).provideVoiceServerUpdate(
                        json.getString("sessionId"),
                        json.getJSONObject("event")
                );
                break;
            case "validationRes":
                ((CoreClientImpl) contextMap.get(webSocket).getCore(getShardId(webSocket, json)).getClient()).provideValidation(
                        json.getString("guildId"),
                        json.optString("channelId"),
                        json.getBoolean("valid")
                );
                break;
            case "isConnectedRes":
                ((CoreClientImpl) contextMap.get(webSocket).getCore(json.getInt("shardId")).getClient()).provideIsConnected(
                        json.getBoolean("connected")
                );
                break;
            default:
                log.warn("Unexpected operation: " + json.getString("op"));
                break;
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        log.error("Caught exception in websocket", e);
    }

    @Override
    public void onStart() {
        log.info("Started WS server with port " + getPort());
    }

    public static void sendPlayerUpdate(WebSocket webSocket, Player player) {
        JSONObject json = new JSONObject();
        json.put("op", "playerUpdate");
        json.put("guildId", player.getGuildId());
        json.put("state", player.getState());

        webSocket.send(json.toString());
    }

    //Shorthand method
    private int getShardId(WebSocket webSocket, JSONObject json) {
        return Util.getShardFromSnowflake(json.getString("guildId"), contextMap.get(webSocket).getShardCount());
    }

    static Collection<SocketContext> getConnections() {
        return contextMap.values();
    }

    public static Map<WebSocket, SocketContext> getContextMap() {
        return contextMap;
    }

    public static Map<String, SocketContext> getContextIdentifierMap() {
        return contextIdentifierMap;
    }

}
