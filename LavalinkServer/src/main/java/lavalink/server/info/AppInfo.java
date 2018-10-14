package lavalink.server.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by napster on 25.06.18.
 * <p>
 * Requires app.properties to be populated with values during the gradle build
 */
@Component
public class AppInfo {

    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);

    private final String version;
    private final String groupId;
    private final String artifactId;
    private final String buildNumber;
    private final long buildTime;

    public AppInfo() {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/app.properties");
        Properties prop = new Properties();
        try {
            prop.load(resourceAsStream);
        } catch (IOException e) {
            log.error("Failed to load app.properties", e);
        }
        this.version = prop.getProperty("version");
        this.groupId = prop.getProperty("groupId");
        this.artifactId = prop.getProperty("artifactId");
        this.buildNumber = prop.getProperty("buildNumber");
        long bTime = -1L;
        try {
            bTime = Long.parseLong(prop.getProperty("buildTime"));
        } catch (NumberFormatException ignored) { }
        this.buildTime = bTime;
    }

    public String getVersion() {
        return this.version;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getBuildNumber() {
        return this.buildNumber;
    }

    public long getBuildTime() {
        return this.buildTime;
    }

    public String getVersionBuild() {
        return this.version + "_" + this.buildNumber;
    }
}
