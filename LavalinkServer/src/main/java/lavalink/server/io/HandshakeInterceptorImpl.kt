package lavalink.server.io

import lavalink.server.config.ServerConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

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
        val version = if (request.uri.path.startsWith("/v3")) 3 else 4
        val password = request.headers.getFirst("Authorization")

        if (password != serverConfig.password) {
            log.error("Authentication failed from ${request.remoteAddress}")
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            return false
        }

        if (request.headers.getFirst("User-Id") == null) {
            log.error("Missing User-Id header from ${request.remoteAddress}")
            response.setStatusCode(HttpStatus.BAD_REQUEST)
            return false
        }

        log.info("Incoming connection from ${request.remoteAddress}")

        val resuming = if (version == 3) {
            val resumeKey = request.headers.getFirst("Resume-Key")
            resumeKey != null && socketServer.canResume(resumeKey)
        } else {
            val sessionId = request.headers.getFirst("Session-Id")
            sessionId != null && socketServer.canResume(sessionId)
        }
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
