package dev.arbjerg.lavalink.protocol

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import java.util.*

data class Info(
    val version: Version,
    val buildTime: Long,
    val git: Git,
    val jvm: String,
    val lavaplayer: String,
    val sourceManagers: List<String>,
    val plugins: List<Plugins>
)

data class Version(
    val semver: String,
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String?,
) {
    companion object {

        private val versionRegex = Regex("""^(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?""")
        fun fromSemver(semver: String): Version {
            val match = versionRegex.matchEntire(semver) ?: return Version(semver, 0, 0, 0, null)
            val major = match.groups["major"]!!.value.toInt()
            val minor = match.groups["minor"]!!.value.toInt()
            val patch = match.groups["patch"]!!.value.toInt()
            val preRelease = match.groups["prerelease"]?.value
            return Version(semver, major, minor, patch, preRelease)
        }
    }
}

data class Git(
    val branch: String,
    val commit: String,
    val commitTime: Long,
)

data class Plugins(val plugins: List<Plugin>)

data class Plugin(val name: String, val version: String)

data class SessionUpdate(
    val resumingKey: Omissible<String?>,
    val timeout: Omissible<Long>
)

data class LoadResult(
    var loadType: ResultStatus,
    var tracks: List<Track>,
    var playlistName: String?,
    var selectedTrack: Int?,
    var exception: Exception?
) {
    constructor(
        loadResultType: ResultStatus,
        tracks: List<Track>,
        playlistName: String?,
        selectedTrack: Int?
    ) : this(
        loadResultType,
        tracks,
        playlistName,
        selectedTrack,
        null
    )

    constructor(exception: Exception?) : this(ResultStatus.LOAD_FAILED, emptyList(), null, null, exception)
}

data class Exception(
    val message: String?,
    val severity: FriendlyException.Severity,
    val cause: String
) {
    constructor(e: FriendlyException) : this(e.message, e.severity, e.toString())
}

enum class ResultStatus {
    TRACK_LOADED,
    PLAYLIST_LOADED,
    SEARCH_RESULT,
    NO_MATCHES,
    LOAD_FAILED
}