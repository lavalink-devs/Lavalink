package lavalink.server.bootstrap

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("metadata")
data class Metadata(
    @XmlElement(true)
    val groupId: String,
    @XmlElement(true)
    val artifactId: String,
    @XmlElement(true)
    val versioning: Versioning
)

@Serializable
@XmlSerialName("versioning")
data class Versioning(
    @XmlElement(true)
    val latest: String,
    @XmlElement(true)
    val release: String,
    @XmlElement(true)
    @XmlChildrenName("version")
    val versions: List<String>,
    @XmlElement(true)
    val lastUpdated: String
)
