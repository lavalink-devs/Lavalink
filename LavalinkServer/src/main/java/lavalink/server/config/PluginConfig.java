package lavalink.server.config;

import lavalink.server.Launcher;
import lavalink.server.plugin.loader.PluginManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties(prefix = "lavalink.plugins")
@Component
public class PluginConfig implements ApplicationContextAware {
    private boolean enabled;
    private Map<String, PluginInfo> locations;
    private AutowireCapableBeanFactory factory;

    public Map<String, PluginInfo> getLocations() {
        return locations;
    }

    public void setLocations(Map<String, PluginInfo> locations) {
        this.locations = locations;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Bean
    public PluginManager manager() {
        PluginManager manager = new PluginManager(Launcher.class.getClassLoader(), factory);
        if(enabled) {
            manager.loadFrom(this);
        }
        return manager;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.factory = applicationContext.getAutowireCapableBeanFactory();
    }
}
