package lavalink.server.config

import lavalink.server.io.HandshakeInterceptorImpl
import lavalink.server.io.SocketServer
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebsocketConfig(
    private val server: SocketServer,
    private val handshakeInterceptor: HandshakeInterceptorImpl,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(server, "/", "/v3/websocket").addInterceptors(handshakeInterceptor)
    }
}
