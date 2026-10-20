package lavalink.server.config

class KoeTunablesConfig {
    var cipherPreferencePolicy: CipherPreferencePolicyType = CipherPreferencePolicyType.HEURISTIC
    var ciphers: List<String> = listOf()
    var daveEnabled: Boolean = true
    var daveLogging: Boolean = true
    var sendSpeakingStop: Boolean = false
    var gatewayConnectTimeoutMs: Long = 10_000
    var highPacketPriority: Boolean = true
    var enableWSSPortOverride: Boolean = false
    var verifyWSSHostname: Boolean = true
}

enum class CipherPreferencePolicyType {
    HEURISTIC,
    SERVER_ORDER,
    PREFERRING,
    BENCHMARK
}
