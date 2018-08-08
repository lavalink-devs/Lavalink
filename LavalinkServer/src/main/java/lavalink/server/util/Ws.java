package lavalink.server.util;

import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.jsr.UndertowSession;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

public class Ws {

    private static final Logger log = LoggerFactory.getLogger(Ws.class);

    public static void send(WebSocketSession session, JSONObject json) {
        UndertowSession undertowSession = (UndertowSession) ((StandardWebSocketSession) session).getNativeSession();
        WebSockets.sendText(json.toString(), undertowSession.getWebSocketChannel(),
                new WebSocketCallback<>() {
                    @Override
                    public void complete(WebSocketChannel channel, Void context) {
                        log.trace("Sent {}", json);
                    }

                    @Override
                    public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                        log.error("Error", throwable);
                    }
                });
    }

}
