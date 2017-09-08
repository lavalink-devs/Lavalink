/*
 * Copyright (c) 2017 Frederik Ar. Mikkelsen & NoobLance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    private String password;

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
}
