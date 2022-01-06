package lavalink.server.config

data class HttpConfig(
        var proxyHost: String = "",
        var proxyPort: Int = 3128,
        var proxyUser: String = "",
        var proxyPassword: String = ""
)