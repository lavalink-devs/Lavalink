package lavalink.server.bootstrap

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class SnapshotUrlTest {
    @TempDir
    lateinit var directory: Path
    private fun withRepository(metadata: String?, test: (String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val body = metadata?.toByteArray() ?: byteArrayOf()
            exchange.sendResponseHeaders(if (metadata == null) 404 else 200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            test("http://127.0.0.1:${server.address.port}/group/plugin/1.0-SNAPSHOT/")
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun timestampedJar() = withRepository("""
        <metadata><versioning><snapshotVersions>
        <snapshotVersion><extension>jar</extension><classifier>sources</classifier><value>1.0-20260101.000000-1</value></snapshotVersion>
        <snapshotVersion><extension>pom</extension><value>1.0-20260101.000000-1</value></snapshotVersion>
        <snapshotVersion><extension>jar</extension><value>1.0-20260101.000000-1</value></snapshotVersion>
        </snapshotVersions></versioning></metadata>
    """.trimIndent()) { base ->
        assertEquals(base + "plugin-1.0-20260101.000000-1.jar",
            resolveSnapshotUrl(base + "plugin-1.0-SNAPSHOT.jar", "plugin", "1.0-SNAPSHOT"))
    }

    @Test
    fun legacySnapshotMetadata() = withRepository("""
        <metadata><versioning><snapshot><timestamp>20260101.000000</timestamp><buildNumber>2</buildNumber></snapshot></versioning></metadata>
    """.trimIndent()) { base ->
        assertEquals(base + "plugin-1.0-20260101.000000-2.jar",
            resolveSnapshotUrl(base + "plugin-1.0-SNAPSHOT.jar", "plugin", "1.0-SNAPSHOT"))
    }

    @Test
    fun missingMetadataKeepsLiteralSnapshot() = withRepository(null) { base ->
        val original = base + "plugin-1.0-SNAPSHOT.jar"
        assertEquals(original, resolveSnapshotUrl(original, "plugin", "1.0-SNAPSHOT"))
    }

    @Test
    fun releaseDoesNotNeedMetadata() {
        val original = "http://127.0.0.1:1/plugin-1.0.jar"
        assertEquals(original, resolveSnapshotUrl(original, "plugin", "1.0"))
    }

    @Test
    fun literalSnapshotMetadata() = withRepository("""
        <metadata><versioning><snapshotVersions><snapshotVersion><extension>jar</extension><value>1.0-SNAPSHOT</value></snapshotVersion></snapshotVersions></versioning></metadata>
    """.trimIndent()) { base ->
        val original = base + "plugin-1.0-SNAPSHOT.jar"
        assertEquals(original, resolveSnapshotUrl(original, "plugin", "1.0-SNAPSHOT"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["../other", "2.0-20260101.000000-1"])
    fun rejectVersionsOutsideTheDeclaredSnapshot(value: String) = withRepository("""
        <metadata><versioning><snapshotVersions><snapshotVersion><extension>jar</extension><value>$value</value></snapshotVersion></snapshotVersions></versioning></metadata>
    """.trimIndent()) { base ->
        assertThrows(IllegalArgumentException::class.java) {
            resolveSnapshotUrl(base + "plugin-1.0-SNAPSHOT.jar", "plugin", "1.0-SNAPSHOT")
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun downloadPlugin(timestamped: Boolean) {
        val bytes = ByteArrayOutputStream()
        JarOutputStream(bytes).use { jar ->
            jar.putNextEntry(JarEntry("lavalink-plugins/plugin.properties"))
            jar.write("name=plugin\npath=test.plugin\nversion=1.0.0-SNAPSHOT\n".toByteArray())
            jar.closeEntry()
        }
        val metadata = """
            <metadata><versioning><snapshotVersions><snapshotVersion><extension>jar</extension><value>1.0.0-20260101.000000-1</value></snapshotVersion></snapshotVersions></versioning></metadata>
        """.trimIndent().toByteArray()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            val isJar = path.endsWith(".jar")
            val body = if (isJar) bytes.toByteArray() else metadata
            exchange.sendResponseHeaders(if (timestamped && path.endsWith("SNAPSHOT.jar")) 404 else 200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        try {
            val config = PluginsConfig().apply {
                pluginsDir = directory.resolve("plugins").toString()
                plugins = listOf(PluginDeclaration("group:plugin:1.0.0-SNAPSHOT",
                    "http://127.0.0.1:${server.address.port}", true))
            }
            val manager = PluginManager(config)
            try {
                assertEquals("1.0.0-SNAPSHOT", manager.pluginManifests.single().version)
                assertEquals(bytes.size().toLong(), Files.size(Path.of(config.pluginsDir, "plugin-1.0.0-SNAPSHOT.jar")))
            } finally {
                (manager.classLoader as URLClassLoader).close()
            }
        } finally {
            server.stop(0)
        }
    }
}
