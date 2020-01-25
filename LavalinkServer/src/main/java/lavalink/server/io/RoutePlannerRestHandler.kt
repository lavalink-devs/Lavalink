package lavalink.server.io

import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
class RoutePlannerRestHandler(private val routePlanner: AbstractRoutePlanner?) {

    /**
     * Returns current information about the active AbstractRoutePlanner
     */
    @GetMapping("/routeplanner/status")
    fun getStatus(request: HttpServletRequest): ResponseEntity<RoutePlannerStatus> {
        val status = when (routePlanner) {
            null -> RoutePlannerStatus(null, null)
            else -> RoutePlannerStatus(
                    routePlanner.javaClass.simpleName,
                    getDetailBlock(routePlanner)
            )
        }
        return ResponseEntity.ok(status)
    }

    /**
     * Removes a single address from the addresses which are currently marked as failing
     */
    @PostMapping("/routeplanner/free/address")
    fun freeSingleAddress(request: HttpServletRequest, @RequestBody requestBody: JSONObject): ResponseEntity<Void> {
        routePlanner ?: throw RoutePlannerDisabledException()
        try {
            val address = InetAddress.getByName(requestBody.getString("address"))
            routePlanner.freeAddress(address)
            return ResponseEntity.noContent().build()
        } catch (exception: UnknownHostException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address: " + exception.message, exception)
        }
    }

    /**
     * Removes all addresses from the list which holds the addresses which are marked failing
     */
    @PostMapping("/routeplanner/free/all")
    fun freeAllAddresses(request: HttpServletRequest): ResponseEntity<Void> {
        routePlanner ?: throw RoutePlannerDisabledException()
        routePlanner.freeAllAddresses()
        return ResponseEntity.noContent().build()
    }

    /**
     * Detail information block for an AbstractRoutePlanner
     */
    private fun getDetailBlock(planner: AbstractRoutePlanner): IRoutePlannerStatus {
        val ipBlock = planner.ipBlock
        val ipBlockStatus = IpBlockStatus(ipBlock.type.simpleName, ipBlock.size.toString() ?: "0")

        val failingAddresses = planner.failingAddresses
        val failingAddressesStatus = failingAddresses.entries.map {
            FailingAddress(it.key, it.value, Date(it.value).toString())
        }

        return when (planner) {
            is RotatingIpRoutePlanner -> RotatingIpRoutePlannerStatus(
                    ipBlockStatus,
                    failingAddressesStatus,
                    planner.rotateIndex.toString(),
                    planner.index.toString(),
                    planner.currentAddress.toString()
            )
            is NanoIpRoutePlanner -> NanoIpRoutePlannerStatus(
                    ipBlockStatus,
                    failingAddressesStatus,
                    planner.currentAddress.toString()
            )
            is RotatingNanoIpRoutePlanner -> RotatingNanoIpRoutePlannerStatus(
                    ipBlockStatus,
                    failingAddressesStatus,
                    planner.currentBlock.toString(),
                    planner.addressIndexInBlock.toString()
            )
            else -> GenericRoutePlannerStatus(ipBlockStatus, failingAddressesStatus)
        }
    }

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

    class RoutePlannerDisabledException : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't access disabled route planner")
}