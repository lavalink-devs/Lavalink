package lavalink.server.io

import dev.arbjerg.lavalink.protocol.*
import lavalink.server.util.logRequest
import lavalink.server.util.socketContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
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
