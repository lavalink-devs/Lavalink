package dev.arbjerg.lavalink.protocol.v3

data class RoutePlannerFreeAddress(val address: String)

data class RoutePlannerStatus(val `class`: String?, val details: IRoutePlannerStatus?)

interface IRoutePlannerStatus
data class GenericRoutePlannerStatus(
    val ipBlock: IpBlockStatus,
    val failingAddresses: List<FailingAddress>
) : IRoutePlannerStatus

data class RotatingIpRoutePlannerStatus(
    val ipBlock: IpBlockStatus,
    val failingAddresses: List<FailingAddress>,
    val rotateIndex: String,
    val ipIndex: String,
    val currentAddress: String
) : IRoutePlannerStatus

data class NanoIpRoutePlannerStatus(
    val ipBlock: IpBlockStatus,
    val failingAddresses: List<FailingAddress>,
    val currentAddressIndex: String
) : IRoutePlannerStatus

data class RotatingNanoIpRoutePlannerStatus(
    val ipBlock: IpBlockStatus,
    val failingAddresses: List<FailingAddress>,
    val blockIndex: String,
    val currentAddressIndex: String
) : IRoutePlannerStatus

data class FailingAddress(val failingAddress: String, val failingTimestamp: Long, val failingTime: String)
data class IpBlockStatus(val type: String, val size: String)

