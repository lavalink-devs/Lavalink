package lavalink.server;

import lavalink.server.io.SocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Controller
public class Launcher {

    public final SocketServer socketServer;

    @Autowired
    public Launcher(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication(Launcher.class);
        sa.setWebEnvironment(false);
        sa.run();
    }

    @Bean
    static SocketServer socketServer(@Value("${lavalink.server.password}") String password) {
        SocketServer ss = new SocketServer(password);
        ss.start();
        return ss;
    }

}
