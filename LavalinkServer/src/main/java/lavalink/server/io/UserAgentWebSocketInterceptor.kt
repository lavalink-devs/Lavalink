package lavalink.server.io

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory

@Component
class UserAgentWebSocketInterceptor : WebSocketHandlerDecoratorFactory {
    override fun decorate(handler: WebSocketHandler): WebSocketHandler {
        return UserAgentWebSocketHandlerDecorator(handler)
    }

    private class UserAgentWebSocketHandlerDecorator(private val delegate: WebSocketHandler) :
        WebSocketHandler by delegate {
        override fun afterConnectionEstablished(session: WebSocketSession) {
            val userAgent = session.handshakeHeaders.getFirst("User-Agent")
            if (userAgent != null) {
                session.attributes["userAgent"] = userAgent
            }
            delegate.afterConnectionEstablished(session)
        }
    }
}