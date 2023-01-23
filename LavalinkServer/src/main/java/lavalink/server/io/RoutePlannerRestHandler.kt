package lavalink.server.io

import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import dev.arbjerg.lavalink.protocol.v3.*
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
    @GetMapping(value = ["/v3/routeplanner/status", "/v4/routeplanner/status"])
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
    @PostMapping(value = ["/v3/routeplanner/free/address", "/v4/routeplanner/free/address"])
    fun freeSingleAddress(
        request: HttpServletRequest,
        @RequestBody body: RoutePlannerFreeAddress
    ): ResponseEntity<Unit> {
        routePlanner ?: throw RoutePlannerDisabledException()
        try {
            val address = InetAddress.getByName(body.address)
            routePlanner.freeAddress(address)
            return ResponseEntity.noContent().build()
        } catch (exception: UnknownHostException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address: " + exception.message, exception)
        }
    }

    /**
     * Removes all addresses from the list which holds the addresses which are marked failing
     */
    @PostMapping(value = ["/v3/routeplanner/free/all", "/v4/routeplanner/free/all"])
    fun freeAllAddresses(request: HttpServletRequest): ResponseEntity<Unit> {
        routePlanner ?: throw RoutePlannerDisabledException()
        routePlanner.freeAllAddresses()
        return ResponseEntity.noContent().build()
    }

    /**
     * Detail information block for an AbstractRoutePlanner
     */
    private fun getDetailBlock(planner: AbstractRoutePlanner): IRoutePlannerStatus {
        val ipBlock = planner.ipBlock
        val ipBlockStatus = IpBlockStatus(ipBlock.type.simpleName, ipBlock.size.toString())

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

    class RoutePlannerDisabledException :
        ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Can't access disabled route planner")

}