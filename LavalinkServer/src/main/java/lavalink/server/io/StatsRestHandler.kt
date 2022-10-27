package lavalink.server.io

import dev.arbjerg.lavalink.protocol.Message
import lavalink.server.util.logRequest
import lavalink.server.util.socketContext
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@RestController
class StatsRestHandler {

    companion object {
        private val log = LoggerFactory.getLogger(StatsRestHandler::class.java)
    }

    @GetMapping("/v3/stats", produces = ["application/json"])
    fun getStats(
        request: HttpServletRequest
    ): ResponseEntity<Message.Stats> {
        logRequest(log, request)

        throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet")
    }
}