package lavalink.server.io

import dev.arbjerg.lavalink.protocol.Message
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class StatsRestHandler {

    @GetMapping("/v3/stats", produces = ["application/json"])
    fun getStats(): ResponseEntity<Message.StatsEvent> {
        throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet")
    }
}