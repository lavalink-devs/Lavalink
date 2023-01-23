package lavalink.server.io

import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class ResponseHeaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.requestURI.startsWith("/v3")) {
            response.addHeader("Lavalink-Api-Version", "3")
        } else {
            response.addHeader("Lavalink-Api-Version", "4")
        }
        filterChain.doFilter(request, response)
    }
}