package lavalink.server.io

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class UserAgentContextFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: jakarta.servlet.FilterChain
    ) {
        try {
            val userAgent = request.getHeader("User-Agent")
            if (userAgent != null) {
                MDC.put("userAgent", userAgent)
            }
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("userAgent")
        }
    }
}