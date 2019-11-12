package lavalink.server.routeplanner.pojo

interface AbstractRoutePlannerStatus
data class GenericRoutePlannerStatus(
        val ipBlock: IpBlockStatus,
        val failingAddresses: List<FailingAddress>
) : AbstractRoutePlannerStatus
data class RotatingIpRoutePlannerStatus(
        val ipBlock: IpBlockStatus,
        val failingAddresses: List<FailingAddress>,
        val rotateIndex: String,
        val ipIndex: String,
        val currentAddress: String
) : AbstractRoutePlannerStatus
data class NanoIpRoutePlannerStatus(
        val ipBlock: IpBlockStatus,
        val failingAddresses: List<FailingAddress>,
        val currentAddressIndex: String
) : AbstractRoutePlannerStatus
data class RotatingNanoIpRoutePlannerStatus(
        val ipBlock: IpBlockStatus,
        val failingAddresses: List<FailingAddress>,
        val blockIndex: String,
        val currentAddressIndex: String
) : AbstractRoutePlannerStatus