package lavalink.server.config

import lavalink.server.io.HandshakeInterceptorImpl
import lavalink.server.io.SocketServer
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
@RestController
class WebsocketConfig(
    private val server: SocketServer,
    private val handshakeInterceptor: HandshakeInterceptorImpl,
) : WebSocketConfigurer {

    companion object {
        private val log = LoggerFactory.getLogger(WebsocketConfig::class.java)
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(server, "/v4/websocket").addInterceptors(handshakeInterceptor)
    }

    @GetMapping("/", "/v3/websocket")
    fun oldWebsocket() {
        log.warn("This is the old Lavalink websocket endpoint. Please use /v4/websocket instead. If you are using a client library, please update it to a Lavalink v4 compatible version or use Lavalink v3 instead.")
    }
}
