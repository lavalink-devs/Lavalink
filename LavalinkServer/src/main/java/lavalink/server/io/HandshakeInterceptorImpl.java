package lavalink.server.io;

import lavalink.server.config.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Objects;

@Controller
public class HandshakeInterceptorImpl implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HandshakeInterceptorImpl.class);
    private final ServerConfig serverConfig;

    @Autowired
    public HandshakeInterceptorImpl(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * Checks credentials and sets the Lavalink version header
     *
     * @return true if authenticated
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        response.getHeaders().add("Lavalink-Major-Version", "3");

        String password = request.getHeaders().getFirst("Authorization");
        boolean matches = Objects.equals(password, serverConfig.getPassword());

        if (matches) {
            log.info("Incoming connection from " + request.getRemoteAddress());
        } else {
            log.error("Authentication failed from " + request.getRemoteAddress());
        }

        return matches;
    }

    // No action required
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {}
}
