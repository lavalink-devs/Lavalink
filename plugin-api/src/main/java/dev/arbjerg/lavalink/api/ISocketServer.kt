package dev.arbjerg.lavalink.api

/**
 * Represents a Lavalink server which handles WebSocket connections.
 */
interface ISocketServer {
    /**
     * A map of all active sessions by their session id.
     */
    val sessions: Map<String, ISocketContext>

    /**
     * A map of all resumable sessions by their session id.
     * A session is resumable if the client configured resuming and has disconnected.
     */
    val resumableSessions: Map<String, ISocketContext>
}