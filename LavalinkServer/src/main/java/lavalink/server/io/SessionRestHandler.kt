package lavalink.server.io

import dev.arbjerg.lavalink.protocol.v3.Session
import dev.arbjerg.lavalink.protocol.v3.SessionUpdate
import dev.arbjerg.lavalink.protocol.v3.takeIfPresent
import lavalink.server.util.socketContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionRestHandler(private val socketServer: SocketServer) {

    @PatchMapping("/v3/sessions/{sessionId}")
    private fun patchSession(
        @RequestBody sessionUpdate: SessionUpdate,
        @PathVariable sessionId: String
    ): ResponseEntity<Session> {
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
