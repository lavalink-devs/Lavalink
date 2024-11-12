package lavalink.server.io

import lavalink.server.config.ServerConfig
import org.pf4j.Extension
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Extension(ordinal = Int.MAX_VALUE) // Register this last, as we need to load plugin configuration contributors first
@Controller
class HandshakeInterceptorImpl @Autowired
constructor(private val serverConfig: ServerConfig, private val socketServer: SocketServer) : HandshakeInterceptor {

    companion object {
        private val log = LoggerFactory.getLogger(HandshakeInterceptorImpl::class.java)
    }

    /**
     * Checks credentials and sets the Lavalink version header
     *
     * @return true if authenticated
     */
    @Suppress("UastIncorrectHttpHeaderInspection")
    override fun beforeHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Boolean {
        val password = request.headers.getFirst("Authorization")

        if (password != serverConfig.password) {
            log.error("Authentication failed from ${request.remoteAddress}")
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        val userId = request.headers.getFirst("User-Id")
        if (userId.isNullOrEmpty() || userId.toLongOrNull() == 0L) {
            log.error("Missing User-Id header from ${request.remoteAddress}")
            response.setStatusCode(HttpStatus.BAD_REQUEST)
            return false
        }

        log.info("Incoming connection from ${request.remoteAddress}")

        val sessionId = request.headers.getFirst("Session-Id")
        val resuming = sessionId != null && socketServer.canResume(sessionId)

        response.headers.add("Session-Resumed", resuming.toString())

        return true
    }

    // No action required
    override fun afterHandshake(
        request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}
