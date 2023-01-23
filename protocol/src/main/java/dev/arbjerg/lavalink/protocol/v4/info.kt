package dev.arbjerg.lavalink.protocol.v4

import com.fasterxml.jackson.annotation.JsonValue

data class Info(
    val version: Version,
    val buildTime: Long,
    val git: Git,
    val jvm: String,
    val lavaplayer: String,
    val sourceManagers: List<String>,
    val filters: List<String>,
    val plugins: Plugins
)

data class Version(
    val semver: String,
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String?,
) {
    companion object {

        private val versionRegex =
            Regex("""^(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?""")

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

data class Plugins(
    @JsonValue
    val plugins: List<Plugin>
)

data class Plugin(val name: String, val version: String)
