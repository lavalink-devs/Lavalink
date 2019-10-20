package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.tools.http.AbstractRoutePlanner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Base64;

@RestController
public final class RoutePlannerRestHandler {

    @GetMapping(value = "/routeplanner/freeIp", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> freeSingleIp(HttpServletRequest request, @RequestParam String ip) {

        final AbstractRoutePlanner activePlanner = AbstractRoutePlanner.getActivePlanner();
        if (activePlanner == null) {
            return ResponseEntity.badRequest().body("No active planner");
        }

        try {
            final InetAddress address = InetAddress.getByAddress(Base64.getDecoder().decode(ip));
            activePlanner.freeAddress(address);
            return ResponseEntity.ok("OK");
        } catch (final Exception ex) {
            return ResponseEntity.badRequest().body("Invalid IP base64");
        }
    }

    @GetMapping(value = "/routeplanner/freeAllIps", produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> freeAllIps(HttpServletRequest request) {

        final AbstractRoutePlanner activePlanner = AbstractRoutePlanner.getActivePlanner();
        if (activePlanner == null) {
            return ResponseEntity.badRequest().body("No active planner");
        }
        activePlanner.freeAllAddresses();
        return ResponseEntity.ok("OK");
    }
}
