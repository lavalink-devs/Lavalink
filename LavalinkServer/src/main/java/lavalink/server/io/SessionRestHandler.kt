package lavalink.server.io

import dev.arbjerg.lavalink.protocol.Session
import dev.arbjerg.lavalink.protocol.SessionUpdate
import dev.arbjerg.lavalink.protocol.takeIfPresent
import lavalink.server.util.logRequest
import lavalink.server.util.socketContext
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class SessionRestHandler(
    private val socketServer: SocketServer,
) {

    companion object {
        private val log = LoggerFactory.getLogger(SessionRestHandler::class.java)
    }

    @PatchMapping("/v3/sessions/{sessionId}", consumes = ["application/json"])
    private fun patchSession(
        request: HttpServletRequest,
        @RequestBody sessionUpdate: SessionUpdate,
        @PathVariable sessionId: String
    ): ResponseEntity<Session>{
        logRequest(log, request)
        val context = socketContext(socketServer, sessionId)

        sessionUpdate.resumingKey.takeIfPresent {
            context.resumeKey = it
        }

        sessionUpdate.timeout.takeIfPresent {
            context.resumeTimeout = it
        }

        return ResponseEntity.ok(Session(context.resumeKey, context.resumeTimeout))
    }

}
