package lavalink.server.config

import lavalink.server.io.HandshakeInterceptorImpl
import lavalink.server.io.SocketServer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception

@Configuration
@EnableWebSocket
@RestController
class WebsocketConfig(private val context: ApplicationContext) : WebSocketConfigurer {

    companion object {
        private val log = LoggerFactory.getLogger(WebsocketConfig::class.java)
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val proxy = WebSocketProxy()
        registry.addHandler(proxy, "/v4/websocket").addInterceptors(proxy)
    }

    @GetMapping("/", "/v3/websocket")
    fun oldWebsocket() {
        log.warn("This is the old Lavalink websocket endpoint. Please use /v4/websocket instead. If you are using a client library, please update it to a Lavalink v4 compatible version or use Lavalink v3 instead.")
    }

    // This is required to lazily evaluate registerWebSocketHandlers()
    inner class WebSocketProxy : TextWebSocketHandler(), HandshakeInterceptor {
        private val socket by lazy { context.getBean<SocketServer>() }
        private val handshaker by lazy { context.getBean<HandshakeInterceptorImpl>() }

        override fun beforeHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            attributes: Map<String, Any>
        ): Boolean = handshaker.beforeHandshake(request, response, wsHandler, attributes)

        override fun afterHandshake(
            request: ServerHttpRequest,
            response: ServerHttpResponse,
            wsHandler: WebSocketHandler,
            exception: Exception?
        ) = handshaker.afterHandshake(request, response, wsHandler, exception)

        override fun handleTextMessage(session: WebSocketSession, message: TextMessage) =
            socket.handleMessage(session, message)

        override fun afterConnectionEstablished(session: WebSocketSession) =
            socket.afterConnectionEstablished(session)

        override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) =
            socket.afterConnectionClosed(session, status)
    }
}
