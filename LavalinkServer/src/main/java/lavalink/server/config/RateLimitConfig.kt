package lavalink.server.config

data class RateLimitConfig(
        var ipBlocks: List<String> = emptyList(),
        var excludedIps: List<String> = emptyList(),
        var strategy: String = "RotateOnBan",
        var retryLimit: Int = -1,
        var searchTriggersFail: Boolean = true
)