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

package lavalink.server.io;

import net.dv8tion.jda.CoreClient;

public class CoreClientImpl implements CoreClient {

    @Override
    public void sendWS(String message) {
        new RuntimeException("sendWS was requested, this shouldn't happen, message:" + message).printStackTrace();
    }

    @Override
    public boolean isConnected() {
        new RuntimeException("isConnected was requested, this shouldn't happen").printStackTrace();
        return true;
    }

    @Override
    public boolean inGuild(String guildId) {
        new RuntimeException("inGuild was requested, this shouldn't happen, guildId:" + guildId).printStackTrace();
        return true;
    }

    @Override
    public boolean voiceChannelExists(String guildId, String channelId) {
        new RuntimeException("voiceChannelExists was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId).printStackTrace();
        return true;
    }

    @Override
    public boolean hasPermissionInChannel(String guildId, String channelId, long l) {
        new RuntimeException("hasPermissionInChannel was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId + " l:" + l).printStackTrace();
        return true;
    }

}
