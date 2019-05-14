package lavalink.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class RequestAuthorizationFilter implements HandlerInterceptor, WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RequestAuthorizationFilter.class);
    private ServerConfig serverConfig;
    private MetricsPrometheusConfigProperties metricsConfig;

    public RequestAuthorizationFilter(ServerConfig serverConfig, MetricsPrometheusConfigProperties metricsConfig) {
        this.serverConfig = serverConfig;
        this.metricsConfig = metricsConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Collecting metrics is anonymous
        if (!metricsConfig.getEndpoint().isEmpty()
                && request.getServletPath().equals(metricsConfig.getEndpoint())) return true;

        if (request.getServletPath().equals("/error")) return true;

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.equals(serverConfig.getPassword())) {
            String method = request.getMethod();
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String ip = request.getRemoteAddr();

            if (authorization == null) {
                log.warn("Authorization missing for {} on {} {}", ip, method, path);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            log.warn("Authorization failed for {} on {} {}", ip, method, path);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }
}
