package lavalink.server.config

import com.sedmelluq.discord.lavaplayer.tools.Ipv4Block
import com.sedmelluq.discord.lavaplayer.tools.Ipv6Block
import com.sedmelluq.discord.lavaplayer.tools.http.AbstractRoutePlanner
import com.sedmelluq.discord.lavaplayer.tools.http.BalancingIpRoutePlanner
import com.sedmelluq.discord.lavaplayer.tools.http.RotatingIpRoutePlanner
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.function.Predicate

@Service
class RoutePlannerService(rateLimitConfig: RateLimitConfig) {


    private final val routePlanner: AbstractRoutePlanner?

    init {
        routePlanner = if(rateLimitConfig.ipBlock.isNotEmpty()) {
            val blacklisted = rateLimitConfig.excludedIps.map { InetAddress.getByName(it) }
            val filter = Predicate<InetAddress> {
                !blacklisted.contains(it)
            }
            val ipBlock = when {
                Ipv4Block.isIpv4CidrBlock(rateLimitConfig.ipBlock) -> Ipv4Block(rateLimitConfig.ipBlock)
                Ipv6Block.isIpv6CidrBlock(rateLimitConfig.ipBlock) -> Ipv6Block(rateLimitConfig.ipBlock)
                else -> throw RuntimeException("Invalid IP Block, make sure to provide a valid CIDR notation")
            }
            when {
                rateLimitConfig.strategy == "RotateOnBan" -> RotatingIpRoutePlanner(ipBlock, filter)
                rateLimitConfig.strategy == "LoadBalance" -> BalancingIpRoutePlanner(ipBlock, filter)
                else -> throw RuntimeException("Invalid strategy, only RotateOnBan and LoadBalance can be used")
            }
        } else {
            null
        }
    }

    @Bean
    fun routePlanner(): AbstractRoutePlanner? {
        return routePlanner
    }
}