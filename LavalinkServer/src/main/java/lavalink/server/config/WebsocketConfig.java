package lavalink.server.config;

import lavalink.server.io.HandshakeInterceptorImpl;
import lavalink.server.io.SocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {

    private final SocketServer server;
    private final HandshakeInterceptorImpl handshakeInterceptor;

    @Autowired
    public WebsocketConfig(SocketServer server, HandshakeInterceptorImpl handshakeInterceptor) {
        this.server = server;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(server, "/")
                .addInterceptors(handshakeInterceptor);
    }
}
