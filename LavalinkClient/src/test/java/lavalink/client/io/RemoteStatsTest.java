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
