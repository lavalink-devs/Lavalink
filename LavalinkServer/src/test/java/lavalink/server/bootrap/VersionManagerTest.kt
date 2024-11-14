package lavalink.server.bootrap

import lavalink.server.bootstrap.FlexibleVersionManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VersionManagerTest {
    private val versionManager = FlexibleVersionManager()

    @Test
    fun `test two commit hashes are compared as equal`() {
        val hash1 = "0d7decb"
        val hash2 = "184367d"

        Assertions.assertEquals(0, versionManager.compareVersions(hash1, hash2))
    }

    @Test
    fun `test commit hash is always higher`() {
        val hash1 = "0d7decb"
        val hash2 = "2.0.0"

        Assertions.assertEquals(1, versionManager.compareVersions(hash1, hash2))
    }

    @Test
    fun `test semver is always lower`() {
        val hash1 = "0d7decb"
        val hash2 = "2.0.0"

        Assertions.assertEquals(-1, versionManager.compareVersions(hash2, hash1))
    }

    @Test
    fun `test hash meets constraint`() {
        val constraint = ">= 2.0.0 && < 3.0.0"
        val hash = "0d7decb"

        Assertions.assertTrue(versionManager.checkVersionConstraint(hash, constraint))
    }

    @Test
    fun `test wilddard constraint`() {
        val constraint = "*"
        val hash = "0d7decb"

        Assertions.assertTrue(versionManager.checkVersionConstraint(hash, constraint))
    }
}
