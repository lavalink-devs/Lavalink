package lavalink.server.bootstrap

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.core.KtXmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlConfig
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.net.HttpURLConnection
import java.net.URL

@Serializable
@XmlSerialName("metadata")
data class Metadata(
    @XmlElement(true)
    val groupId: String = "",
    @XmlElement(true)
    val artifactId: String = "",
    @XmlElement(true)
    val versioning: Versioning = Versioning(),
)

@Serializable
@XmlSerialName("versioning")
data class Versioning(
    @XmlElement(true)
    val latest: String = "",
    @XmlElement(true)
    val release: String = "",
    @XmlElement(true)
    @XmlChildrenName("version")
    val versions: List<String> = emptyList(),
    @XmlElement(true)
    val lastUpdated: String = "",
    @XmlElement(true)
    val snapshot: Snapshot? = null,
    @XmlElement(true)
    @XmlChildrenName("snapshotVersion")
    val snapshotVersions: List<SnapshotVersion> = emptyList(),
)

@Serializable
@XmlSerialName("snapshot")
data class Snapshot(
    @XmlElement(true) val timestamp: String = "",
    @XmlElement(true) val buildNumber: String = "",
)

@Serializable
@XmlSerialName("snapshotVersion")
data class SnapshotVersion(
    @XmlElement(true) val extension: String = "",
    @XmlElement(true) val classifier: String = "",
    @XmlElement(true) val value: String = "",
)

internal fun resolveSnapshotUrl(artifactUrl: String, name: String, version: String): String {
    if (!version.endsWith("-SNAPSHOT")) return artifactUrl

    val directory = artifactUrl.substringBeforeLast('/') + "/"
    val connection = URL(directory + "maven-metadata.xml").openConnection() as HttpURLConnection
    connection.connectTimeout = 5000
    connection.readTimeout = 5000
    val metadata = try {
        // Repositories that publish literal SNAPSHOT jars need no version metadata.
        if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return artifactUrl
        connection.inputStream.use { xml.decodeFromReader(Metadata.serializer(), KtXmlReader(it)) }
    } finally {
        connection.disconnect()
    }

    val versioning = metadata.versioning
    val resolvedVersion = versioning.snapshotVersions
        .firstOrNull { it.extension == "jar" && it.classifier.isEmpty() }
        ?.value
        ?: versioning.snapshot?.takeIf {
            versioning.snapshotVersions.isEmpty() && it.timestamp.isNotEmpty() && it.buildNumber.isNotEmpty()
        }?.let { version.removeSuffix("SNAPSHOT") + it.timestamp + "-" + it.buildNumber }
        ?: return artifactUrl

    if (resolvedVersion == version) return artifactUrl

    // Metadata selects a version within this directory, not an arbitrary URL.
    val suffix = resolvedVersion.removePrefix(version.removeSuffix("SNAPSHOT"))
    require(resolvedVersion.startsWith(version.removeSuffix("SNAPSHOT")) &&
        Regex("\\d{8}\\.\\d{6}-\\d+").matches(suffix)) {
        "Invalid snapshot version '$resolvedVersion' for $name:$version"
    }
    return directory + "$name-$resolvedVersion.jar"
}

val xml = XML {
    defaultPolicy {
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    }
}
