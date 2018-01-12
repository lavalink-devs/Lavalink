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

package lavalink.server.util;


import net.dv8tion.jda.audio.hooks.ConnectionListener;
import net.dv8tion.jda.audio.hooks.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 65,42% copy pasta from the DebugConnectionListener in FredBoat
 */
public class DebugConnectionListener implements ConnectionListener {

    private static final Logger log = LoggerFactory.getLogger(DebugConnectionListener.class);

    private ConnectionStatus oldStatus = null;
    private final String guildId;

    public DebugConnectionListener(String guildId) {
        this.guildId = guildId;
    }

    @Override
    public void onPing(long l) {

    }

    @Override
    public void onStatusChange(ConnectionStatus connectionStatus) {
        log.debug("Status change for audio connection in guild {} : {} => {}",
                guildId, oldStatus, connectionStatus);

        oldStatus = connectionStatus;
    }

    @Override
    public void onUserSpeaking(String s, boolean b) {

    }
}
