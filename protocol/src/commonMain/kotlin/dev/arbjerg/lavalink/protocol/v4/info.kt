package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * Representation of LavaLink server information.
 *
 * @property version the [Version] of this Lavalink server
 * @property buildTime the millisecond unix timestamp when this Lavalink jar was built
 * @property git the [git information][Git] of this Lavalink server
 * @property jvm the JVM version this Lavalink server runs on
 * @property lavaplayer the Lavaplayer version being used by this server
 * @property sourceManagers the enabled source managers for this server
 * @property filters the enabled filters for this server
 * @property plugins the enabled [Plugins][Plugin] for this server
 */
@Serializable
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

/**
 * Representation of a Lavalink version.
 *
 * @property semver the full version string of this Lavalink server
 * @property major the major version of this Lavalink server
 * @property minor the minor version of this Lavalink server
 * @property patch the patch version of this Lavalink server
 * @property preRelease the pre-release version according to semver as a . separated list of identifiers
 */
@Serializable
data class Version(
    val semver: String,
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String?,
) {
    companion object {
        private val versionRegex =
            """^(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?""".toRegex()

        /**
         * Creates a [Version] object from a semver string.
         */
        fun fromSemver(semver: String): Version {
            val match = versionRegex.matchEntire(semver) ?: return Version(semver, 0, 0, 0, null)
            val (major, minor, patch, preRelease) = match.destructured
            return Version(semver, major.toInt(), minor.toInt(), patch.toInt(), preRelease)
        }
    }
}

@Serializable
data class Git(
    val branch: String,
    val commit: String,
    val commitTime: Long,
)

@JvmInline
@Serializable(with = Plugins.Serializer::class)
value class Plugins(val plugins: List<Plugin>) {

    // https://youtrack.jetbrains.com/issue/KT-57647/Exception-when-deserializing-a-sealed-family-value-class-member
    companion object Serializer : KSerializer<Plugins> {
        private val parent = ListSerializer(Plugin.serializer())
        override val descriptor: SerialDescriptor
            get() = parent.descriptor

        override fun deserialize(decoder: Decoder): Plugins =
            Plugins(decoder.decodeInline(descriptor).decodeSerializableValue(parent))

        override fun serialize(encoder: Encoder, value: Plugins) =
            encoder.encodeInline(descriptor).encodeSerializableValue(parent, value.plugins)
    }
}

@Serializable
data class Plugin(val name: String, val version: String)
