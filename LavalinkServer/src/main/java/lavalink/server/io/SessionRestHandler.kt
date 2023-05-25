package lavalink.server.io

import dev.arbjerg.lavalink.protocol.v4.Session
import dev.arbjerg.lavalink.protocol.v4.SessionUpdate
import dev.arbjerg.lavalink.protocol.v4.ifPresent
import lavalink.server.util.socketContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionRestHandler(private val socketServer: SocketServer) {

    @PatchMapping("/v4/sessions/{sessionId}")
    private fun patchSession(
        @RequestBody sessionUpdate: SessionUpdate,
        @PathVariable sessionId: String
    ): ResponseEntity<Session> {
        val context = socketContext(socketServer, sessionId)

        sessionUpdate.resuming.ifPresent {
            context.resumable = it
        }

        sessionUpdate.timeout.ifPresent {
            context.resumeTimeout = it.inWholeSeconds
        }

        return ResponseEntity.ok(Session(context.resumable, context.resumeTimeout))
    }

}
