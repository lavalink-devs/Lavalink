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

    override fun toString(): String {
        var baseSemver = "${major}.${minor}.${patch}"

        if(!preRelease.isNullOrEmpty())
            baseSemver += "-${preRelease}"

        return baseSemver
    }

    operator fun compareTo(other: Version): Int {
        // Compare major, minor, and patch
        val majorDiff = major - other.major
        if (majorDiff != 0) return majorDiff

        val minorDiff = minor - other.minor
        if (minorDiff != 0) return minorDiff

        val patchDiff = patch - other.patch
        if (patchDiff != 0) return patchDiff

        // Compare prerelease (null means no prerelease and is greater)
        return when {
            preRelease.isNullOrEmpty() && other.preRelease.isNullOrEmpty() -> 0
            preRelease.isNullOrEmpty() && !other.preRelease.isNullOrEmpty() -> 1
            !preRelease.isNullOrEmpty() && other.preRelease.isNullOrEmpty() -> -1
            !preRelease.isNullOrEmpty() && !other.preRelease.isNullOrEmpty() -> comparePreRelease(preRelease, other.preRelease)
            else -> 0
        }
    }

    private fun comparePreRelease(part1: String, part2: String): Int {
        val components1 = part1.split(".")
        val components2 = part2.split(".")
        val maxLength = maxOf(components1.size, components2.size)

        for (i in 0 until maxLength) {
            val comp1 = components1.getOrNull(i)
            val comp2 = components2.getOrNull(i)

            if (comp1 == null) return -1 // `part1` is shorter and considered smaller
            if (comp2 == null) return 1  // `part2` is shorter and considered smaller

            val isNumeric1 = comp1.all { it.isDigit() }
            val isNumeric2 = comp2.all { it.isDigit() }

            when {
                isNumeric1 && isNumeric2 -> {
                    // Compare numerically
                    val diff = comp1.toInt() - comp2.toInt()
                    if (diff != 0) return diff
                }
                isNumeric1 -> return -1 // Numeric parts come before string parts
                isNumeric2 -> return 1  // String parts come after numeric parts
                else -> {
                    // Compare lexicographically
                    val diff = comp1.compareTo(comp2)
                    if (diff != 0) return diff
                }
            }
        }

        return 0 // Parts are equal
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
