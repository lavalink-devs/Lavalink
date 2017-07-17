package lavalink.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "lavalink.server")
@Component
public class Config {

    private final Sources sources = new Sources();

    public Sources getSources() {
        return sources;
    }

    private String userId;

    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class Sources {

        private boolean youtube = true;
        private boolean bandcamp = true;
        private boolean soundcloud = true;
        private boolean twitch = true;
        private boolean vimeo = true;
        private boolean http = true;

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

        public boolean isHttp() {
            return http;
        }

        public void setHttp(boolean http) {
            this.http = http;
        }
    }
}
