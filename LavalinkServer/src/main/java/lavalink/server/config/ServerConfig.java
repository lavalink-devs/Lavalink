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

package lavalink.server.config;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "lavalink.server")
@Component
public class ServerConfig {

    private String password;
    private String sentryDsn = "";
    @Nullable
    private Integer bufferDurationMs;
    @Nullable
    private Integer youtubePlaylistLoadLimit;
    private boolean gcWarnings = true;
    private boolean youtubeSearchEnabled = true;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @deprecated use {@link SentryConfigProperties} instead.
     */
    @Deprecated
    public String getSentryDsn() {
        return sentryDsn;
    }

    public void setSentryDsn(String sentryDsn) {
        this.sentryDsn = sentryDsn;
    }

    @Nullable
    public Integer getBufferDurationMs() {
        return bufferDurationMs;
    }

    public void setBufferDurationMs(@Nullable Integer bufferDurationMs) {
        this.bufferDurationMs = bufferDurationMs;
    }

    @Nullable
    public Integer getYoutubePlaylistLoadLimit() {
        return youtubePlaylistLoadLimit;
    }

    public void setYoutubePlaylistLoadLimit(@Nullable Integer youtubePlaylistLoadLimit) {
        this.youtubePlaylistLoadLimit = youtubePlaylistLoadLimit;
    }

    public boolean isGcWarnings() {
        return gcWarnings;
    }

    public void setGcWarnings(boolean gcWarnings) {
        this.gcWarnings = gcWarnings;
    }

    public boolean isYoutubeSearchEnabled() {
        return youtubeSearchEnabled;
    }

    public void setYoutubeSearchEnabled(boolean youtubeSearchEnabled) {
        this.youtubeSearchEnabled = youtubeSearchEnabled;
    }
}
