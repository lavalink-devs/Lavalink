package dev.arbjerg.lavalink.protocol

import org.apache.http.HttpStatus
import java.time.Instant

data class Error(
    val timestamp: Instant,
    val status: HttpStatus,
    val error: String,
    val trace: String? = null,
    val message: String,
    val path: String
)