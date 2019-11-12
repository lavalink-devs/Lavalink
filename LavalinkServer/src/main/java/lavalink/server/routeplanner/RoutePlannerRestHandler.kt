package lavalink.server.routeplanner

import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import lavalink.server.routeplanner.pojo.*
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
class RoutePlannerRestHandler(val routePlanner: AbstractRoutePlanner?) {

    /**
     * Returns current information about the active AbstractRoutePlanner
     */
    @GetMapping(value = ["/routeplanner/status"], produces = ["application/json"])
    fun getStatus(request: HttpServletRequest): ResponseEntity<RoutePlannerStatus> {
        if (routePlanner == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No route planner enabled")
        }
        return ResponseEntity.ok(
                RoutePlannerStatus(routePlanner?.javaClass?.canonicalName ?: "", getDetailBlock(routePlanner)))
    }

    /**
     * Removes a single address from the addresses which are currently marked as failing
     */
    @PostMapping(value = ["/routeplanner/free/address"], consumes = ["application/json"])
    fun freeSingleAddress(request: HttpServletRequest, @RequestBody requestBody: JSONObject): ResponseEntity<Void> {
        if (routePlanner == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No route planner enabled")
        }
        try {
            val address = InetAddress.getByName(requestBody.getString("address"))
            routePlanner!!.freeAddress(address)
            return ResponseEntity.noContent().build()
        } catch (exception: UnknownHostException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid address: " + exception.message, exception)
        }
    }

    /**
     * Removes all addresses from the list which holds the addresses which are marked failing
     */
    @PostMapping(value = ["/routeplanner/free/all"])
    fun freeAllAddresses(request: HttpServletRequest): ResponseEntity<Void> {
        if (routePlanner == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No route planner enabled")
        }
        routePlanner!!.freeAllAddresses()
        return ResponseEntity.noContent().build()
    }

    /**
     * Detail information block for an AbstractRoutePlanner
     */
    private fun getDetailBlock(planner: AbstractRoutePlanner?): AbstractRoutePlannerStatus {

        val ipBlock = planner?.ipBlock
        val ipBlockStatus = IpBlockStatus(ipBlock?.type?.simpleName ?: "", ipBlock?.size?.toString() ?: "0")

        val failingAddresses = planner?.failingAddresses
        val failingAddressesStatus = mutableListOf<FailingAddress>()
        failingAddresses?.entries?.forEach {
            val failingAddress = FailingAddress(it.key, it.value, Date(it.value).toString())
            failingAddressesStatus.add(failingAddress)
        }

        return when (planner) {
            is RotatingIpRoutePlanner -> {
                RotatingIpRoutePlannerStatus(
                        ipBlockStatus,
                        failingAddressesStatus,
                        planner.rotateIndex.toString(),
                        planner.index.toString(),
                        planner.currentAddress.toString()
                )
            }
            is NanoIpRoutePlanner -> {
                NanoIpRoutePlannerStatus(
                        ipBlockStatus,
                        failingAddressesStatus,
                        planner.currentAddress.toString()
                )
            }
            is RotatingNanoIpRoutePlanner -> {
                RotatingNanoIpRoutePlannerStatus(
                        ipBlockStatus,
                        failingAddressesStatus,
                        planner.currentBlock.toString(),
                        planner.addressIndexInBlock.toString()
                )
            }
            else -> {
                GenericRoutePlannerStatus(ipBlockStatus, failingAddressesStatus)
            }
        }
    }
}