package lavalink.server.config;

import lavalink.server.Launcher;
import lavalink.server.plugin.loader.PluginManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "lavalink.plugins")
@Component
public class PluginConfig {
    private List<String> paths = new ArrayList<>();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Bean
    public PluginManager manager() {
        PluginManager manager = new PluginManager(Launcher.class.getClassLoader());
        manager.loadFrom(this);
        return manager;
    }
}
