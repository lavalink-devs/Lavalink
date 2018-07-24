package lavalink.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by napster on 20.05.18.
 */
@Component
@ConfigurationProperties(prefix = "sentry")
public class SentryConfigProperties {

    private String dsn = "";
    private Map<String, String> tags = new HashMap<>();

    public String getDsn() {
        return dsn;
    }

    public void setDsn(String dsn) {
        this.dsn = dsn;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
