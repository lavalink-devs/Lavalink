package lavalink.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by napster on 20.05.18.
 */
@Component
@ConfigurationProperties("metrics.prometheus")
public class MetricsPrometheusConfigProperties {

    private boolean enabled = false;
    private String endpoint = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
