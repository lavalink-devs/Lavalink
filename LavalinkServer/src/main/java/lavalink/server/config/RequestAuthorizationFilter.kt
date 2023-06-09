package lavalink.server.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class RequestAuthorizationFilter(
    private val serverConfig: ServerConfig,
    private val metricsConfig: MetricsPrometheusConfigProperties
) : HandlerInterceptor, WebMvcConfigurer {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        return when {
            // Collecting metrics is anonymous
            metricsConfig.endpoint.isNotEmpty() && request.servletPath == metricsConfig.endpoint -> true

            request.servletPath == "/error" -> true

            else -> {
                val authorization = request.getHeader("Authorization")
                if (authorization == null || authorization != serverConfig.password) {
                    val path = request.requestURI.substring(request.contextPath.length)
                    if (authorization == null) {
                        log.warn("Authorization missing for ${request.remoteAddr} on ${request.method} $path")
                        response.status = HttpStatus.UNAUTHORIZED.value()
                        return false
                    }

                    log.warn("Authorization failed for ${request.remoteAddr} on ${request.method} $path")
                    response.status = HttpStatus.FORBIDDEN.value()
                    return false
                }

                return true
            }
        }
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(this)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RequestAuthorizationFilter::class.java)
    }
}
