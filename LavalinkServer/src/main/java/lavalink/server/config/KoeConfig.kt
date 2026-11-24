package lavalink.server.config

class KoeConfig {
    var cipherPreferencePolicy: CipherPreferencePolicyType = CipherPreferencePolicyType.HEURISTIC
    var ciphers: List<String> = listOf()
    var daveLogging: Boolean = true
    var gatewayConnectTimeoutMs: Long = 10_000
}

enum class CipherPreferencePolicyType {
    HEURISTIC,
    SERVER_ORDER,
    PREFERRING,
    BENCHMARK
}
