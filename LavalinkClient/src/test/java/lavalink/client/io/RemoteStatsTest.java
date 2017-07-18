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

package lavalink.client.io;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RemoteStatsTest {

    private static final Logger log = LoggerFactory.getLogger(RemoteStatsTest.class);

    @Test
    void remoteStatsTest() {
        JSONObject json = new JSONObject("{\"playingPlayers\":0,\"op\":\"stats\",\"memory\":{\"reservable\":1892155392,\"used\":67111552,\"free\":137885056,\"allocated\":204996608},\"players\":0,\"cpu\":{\"cores\":4,\"systemLoad\":0,\"lavalinkLoad\":0},\"uptime\":15754}");
        RemoteStats stats = new RemoteStats(json);

        Assertions.assertEquals(-1, stats.getAvgFramesSentPerMinute());
        Assertions.assertEquals(-1, stats.getAvgFramesNulledPerMinute());
        Assertions.assertEquals(-1, stats.getAvgFramesDeficitPerMinute());
        log.info(stats + "");
    }

}
