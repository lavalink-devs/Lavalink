package lavalink.server.io;

import lavalink.server.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestAuthorizationFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestAuthorizationFilter.class);
    private ServerConfig serverConfig;

    public RequestAuthorizationFilter(ServerConfig config) {
        this.serverConfig = config;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.equals(serverConfig.getPassword())) {
            log.warn("Authorization failed");
            response.setStatus(401);
            return false;
        }

        return true;
    }
}
