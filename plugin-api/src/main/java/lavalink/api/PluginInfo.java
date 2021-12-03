package lavalink.api;

import org.json.JSONObject;

/**
 * Name and version info about a plugin. Must be provided as a bean.
 */
public interface PluginInfo {
    int getMajor();
    int getMinor();
    int getPatch();
    String getName();
    default JSONObject getSupplementalData() {
        return new JSONObject();
    }
}
