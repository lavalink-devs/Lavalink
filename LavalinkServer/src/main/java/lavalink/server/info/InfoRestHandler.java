package lavalink.server.info;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by napster on 08.03.19.
 */
@RestController
public class InfoRestHandler {

    private final AppInfo appInfo;

    public InfoRestHandler(AppInfo appInfo) {
        this.appInfo = appInfo;
    }

    @GetMapping("/version")
    public String version() {
        return appInfo.getVersionBuild();
    }
}
