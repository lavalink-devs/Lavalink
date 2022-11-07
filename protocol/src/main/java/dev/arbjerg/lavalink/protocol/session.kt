package dev.arbjerg.lavalink.protocol


data class Session(
    val resumingKey: String? = null,
    val timeout: Long,
)

data class SessionUpdate(
    var resumingKey: Omissible<String>? = Omissible.omitted(),
    var timeout: Omissible<Long> = Omissible.omitted(),
)