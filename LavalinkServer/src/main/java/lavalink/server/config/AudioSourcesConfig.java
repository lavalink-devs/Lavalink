package lavalink.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by napster on 05.03.18.
 */
@ConfigurationProperties(prefix = "lavalink.server.sources")
@Component
public class AudioSourcesConfig {

    private boolean youtube = true;
    private boolean bandcamp = true;
    private boolean soundcloud = true;
    private boolean twitch = true;
    private boolean vimeo = true;
    private boolean mixer = true;
    private boolean http = true;
    private boolean local = false;

    public boolean isYoutube() {
        return youtube;
    }

    public void setYoutube(boolean youtube) {
        this.youtube = youtube;
    }

    public boolean isBandcamp() {
        return bandcamp;
    }

    public void setBandcamp(boolean bandcamp) {
        this.bandcamp = bandcamp;
    }

    public boolean isSoundcloud() {
        return soundcloud;
    }

    public void setSoundcloud(boolean soundcloud) {
        this.soundcloud = soundcloud;
    }

    public boolean isTwitch() {
        return twitch;
    }

    public void setTwitch(boolean twitch) {
        this.twitch = twitch;
    }

    public boolean isVimeo() {
        return vimeo;
    }

    public void setVimeo(boolean vimeo) {
        this.vimeo = vimeo;
    }

    public boolean isMixer() {
        return mixer;
    }

    public void setMixer(boolean mixer) {
        this.mixer = mixer;
    }

    public boolean isHttp() {
        return http;
    }

    public void setHttp(boolean http) {
        this.http = http;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }
}
