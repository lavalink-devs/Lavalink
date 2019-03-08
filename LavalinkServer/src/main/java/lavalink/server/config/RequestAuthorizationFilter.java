package lavalink.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class RequestAuthorizationFilter implements HandlerInterceptor, WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RequestAuthorizationFilter.class);
    private ServerConfig serverConfig;

    public RequestAuthorizationFilter(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: Log IP
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.equals(serverConfig.getPassword())) {
            String method = request.getMethod();
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String ip = request.getRemoteAddr();

            log.warn("Authorization failed for " + ip + " on " + method + " " + path);
            response.setStatus(401);
            return false;
        }

        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }
}
