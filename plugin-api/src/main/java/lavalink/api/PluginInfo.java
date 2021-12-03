package lavalink.api;

import java.util.Collections;
import java.util.Map;

/**
 * Name and version info about a plugin. Must be provided as a bean.
 */
public interface PluginInfo {
    int getMajor();
    int getMinor();
    int getPatch();
    String getName();
    default Map<Object, Object> getSupplementalData() {
        return Collections.emptyMap();
    }
}
