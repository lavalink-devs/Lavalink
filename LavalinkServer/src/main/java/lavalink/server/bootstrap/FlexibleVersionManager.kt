package lavalink.server.bootstrap

import org.pf4j.VersionManager
import com.github.zafarkhaja.semver.Version

private val commitHashRegex = Regex("^[0-9a-fA-F]{7,40}$")

/**
 * Implementation of [VersionManager] which also accepts commit hashes as versions.
 */
class FlexibleVersionManager : VersionManager {
    override fun checkVersionConstraint(version: String, constraint: String): Boolean {
        if (constraint == "*") return true
        return if(Version.isValid(version)) {
            Version.parse(version).satisfies(constraint)
        } else {
            return version.matches(commitHashRegex)
        }
    }

    override fun compareVersions(v1: String, v2: String): Int {
        if (v1.matches(commitHashRegex)) {
            // Commit hashes cannot be compared
            if (v2.matches(commitHashRegex)) return 0
            // Commit hash should always win
            return 1
        }
        // Commit hash should always win
        if (v2.matches(commitHashRegex)) return -1
        return Version.parse(v1).compareTo(Version.parse(v2))
    }
}