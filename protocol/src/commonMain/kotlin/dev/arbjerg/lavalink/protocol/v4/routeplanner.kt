@file:Suppress("DataClassPrivateConstructor")

package dev.arbjerg.lavalink.protocol.v4

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * [SerializersModule] containing configuration for [RoutePlannerStatus].
 */
val RoutePlannerModule = SerializersModule {
    polymorphic(RoutePlannerStatus::class) {
        subclass(RoutePlannerStatus.RotatingIpRoutePlannerStatus.serializer())
        subclass(RoutePlannerStatus.NanoIpRoutePlannerStatus.serializer())
        subclass(RoutePlannerStatus.RotatingNanoIpRoutePlannerStatus.serializer())
        subclass(RoutePlannerStatus.BalancingIpRoutePlannerStatus.serializer())
    }
}

@Serializable
data class RoutePlannerFreeAddress(val address: String)

@JsonClassDiscriminator("class")
@Serializable
sealed interface RoutePlannerStatus {
    val `class`: Class
    val details: Details

    sealed interface Details {
        val ipBlock: IpBlockStatus
        val failingAddresses: List<FailingAddress>
    }

    @Serializable
    enum class Class {
        @SerialName("RotatingIpRoutePlanner")
        ROTATING_IP_ROUTE_PLANNER,

        @SerialName("NanoIpRoutePlanner")
        NANO_IP_ROUTE_PLANNER,

        @SerialName("RotatingNanoIpRoutePlanner")
        ROTATING_NANO_IP_ROUTE_PLANNER,

        @SerialName("BalancingIpRoutePlanner")
        BALANCING_IP_ROUTE_PLANNER
    }

    @Serializable
    @SerialName("RotatingIpRoutePlanner")
    data class RotatingIpRoutePlannerStatus(
        override val details: Details
    ) : RoutePlannerStatus {
        @Transient
        override val `class`: Class = Class.ROTATING_IP_ROUTE_PLANNER

        @Serializable
        data class Details(
            override val ipBlock: IpBlockStatus,
            override val failingAddresses: List<FailingAddress>,
            val rotateIndex: String,
            val ipIndex: String,
            val currentAddress: String
        ) : RoutePlannerStatus.Details
    }

    @Serializable
    @SerialName("NanoIpRoutePlanner")
    data class NanoIpRoutePlannerStatus(
        override val details: Details
    ) : RoutePlannerStatus {
        @Transient
        override val `class`: Class = Class.NANO_IP_ROUTE_PLANNER

        @Serializable
        data class Details(
            override val ipBlock: IpBlockStatus,
            override val failingAddresses: List<FailingAddress>,
            val currentAddressIndex: String
        ) : RoutePlannerStatus.Details
    }

    @Serializable
    @SerialName("RotatingNanoIpRoutePlanner")
    data class RotatingNanoIpRoutePlannerStatus(
        override val details: Details
    ) : RoutePlannerStatus {

        @Transient
        override val `class`: Class = Class.ROTATING_NANO_IP_ROUTE_PLANNER

        @Serializable
        data class Details(
            override val ipBlock: IpBlockStatus,
            override val failingAddresses: List<FailingAddress>,
            val blockIndex: String,
            val currentAddressIndex: String
        ) : RoutePlannerStatus.Details
    }

    @Serializable
    @SerialName("BalancingIpRoutePlanner")
    data class BalancingIpRoutePlannerStatus(
        override val details: Details
    ) : RoutePlannerStatus {
        @Transient
        override val `class`: Class = Class.BALANCING_IP_ROUTE_PLANNER

        @Serializable
        data class Details(
            override val ipBlock: IpBlockStatus,
            override val failingAddresses: List<FailingAddress>
        ) : RoutePlannerStatus.Details
    }

    @Serializable
    data class FailingAddress(val failingAddress: String, val failingTimestamp: Long, val failingTime: String)

    @Serializable
    data class IpBlockStatus(val type: Type, val size: String) {
        @Serializable
        enum class Type {
            @SerialName("Inet4Address")
            INET_4_ADDRESS,

            @SerialName("Inet6Address")
            INET_6_ADDRESS,
        }
    }
}

