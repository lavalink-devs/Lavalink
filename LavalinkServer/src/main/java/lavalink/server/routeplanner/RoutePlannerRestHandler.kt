package lavalink.server.routeplanner

import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    fun getStatus(request: HttpServletRequest): ResponseEntity<String> {
        val jsonObject = JSONObject()
        if (routePlanner == null) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "No route planner enabled")
            return ResponseEntity.badRequest().body(jsonObject.toString())
        }
        jsonObject.put("status", "OK")
        jsonObject.put("class", routePlanner?.javaClass?.canonicalName ?: "Error")
        jsonObject.put("details", getDetailBlock(routePlanner))
        return ResponseEntity.ok(jsonObject.toString())
    }

    /**
     * Removes a single address from the addresses which are currently marked as failing
     */
    @PostMapping(value = ["/routeplanner/free/address"], consumes = ["application/json"], produces = ["application/json"])
    fun freeSingleAddress(request: HttpServletRequest, @RequestBody requestBody: String): ResponseEntity<String> {
        val jsonObject = JSONObject()
        if (routePlanner == null) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "No route planner enabled")
            return ResponseEntity.badRequest().body(jsonObject.toString())
        }
        return try {
            val body = JSONObject(requestBody)
            val address = InetAddress.getByName(body.getString("address"))
            routePlanner?.freeAddress(address)
            jsonObject.put("status", "OK")
            jsonObject.put("message", "address successfully unmarked")
            ResponseEntity.ok(jsonObject.toString())
        } catch (exception: JSONException) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "Invalid request body: " + exception.message)
            ResponseEntity.badRequest().body(jsonObject.toString())
        } catch (exception: UnknownHostException) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "Invalid address: " + exception.message)
            ResponseEntity.badRequest().body(jsonObject.toString())
        }
    }

    /**
     * Removes all addresses from the list which holds the addresses which are marked failing
     */
    @PostMapping(value = ["/routeplanner/free/all"], consumes = ["application/json"], produces = ["application/json"])
    fun freeAllAddresses(request: HttpServletRequest, @RequestBody requestBody: String): ResponseEntity<String> {
        val jsonObject = JSONObject()
        if (routePlanner == null) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "No route planner enabled")
            return ResponseEntity.badRequest().body(jsonObject.toString())
        }
        return try {
            routePlanner?.freeAllAddresses()
            jsonObject.put("status", "OK")
            jsonObject.put("message", "address successfully unmarked")
            ResponseEntity.ok(jsonObject.toString())
        } catch (exception: JSONException) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "Invalid request body: " + exception.message)
            ResponseEntity.badRequest().body(jsonObject.toString())
        } catch (exception: UnknownHostException) {
            jsonObject.put("status", "E")
            jsonObject.put("message", "Invalid address: " + exception.message)
            ResponseEntity.badRequest().body(jsonObject.toString())
        }
    }

    /**
     * Detail information block for an AbstractRoutePlanner
     */
    private fun getDetailBlock(planner: AbstractRoutePlanner?): JSONObject {
        val jsonObject = JSONObject()

        if (planner is RotatingIpRoutePlanner) {
            jsonObject.put("rotateIndex", planner.rotateIndex)
            jsonObject.put("ipIndex", planner.index)
            jsonObject.put("currentAddress", planner.currentAddress?.toString())
        }

        if(planner is NanoIpRoutePlanner) {
            jsonObject.put("estAddressIndex", planner.currentAddress)
        }

        if(planner is RotatingNanoIpRoutePlanner) {
            jsonObject.put("blockIndex", planner.currentBlock.longValueExact())
            jsonObject.put("estAddressIndex", planner.addressIndexInBlock)
        }

        val ipBlock = planner?.ipBlock
        val ipBlockJson = JSONObject()
        ipBlockJson.put("type", ipBlock?.type?.simpleName ?: "Error")
        ipBlockJson.put("size", ipBlock?.size ?: 0)

        val failingAddresses = planner?.failingAddresses
        val failingAddressesJson = JSONArray()
        failingAddresses?.entries?.forEach {
            val failingAdressData = JSONObject()
            failingAdressData.put("address", it.key)
            failingAdressData.put("failingTimestamp", it.value)
            failingAdressData.put("failingTime", Date(it.value).toString())
            failingAddressesJson.put(failingAdressData)
        }

        jsonObject.put("ipBlock", ipBlockJson)
        jsonObject.put("failingAddresses", failingAddressesJson)
        return jsonObject
    }
}