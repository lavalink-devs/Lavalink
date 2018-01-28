package lavalink.server.io;

import net.dv8tion.jda.manager.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class ConnectionManagerImpl implements ConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(ConnectionManagerImpl.class);

    @Override
    public void removeAudioConnection(String guildId) {
    }

    @Override
    public void queueAudioConnect(String guildId, String channelId) {
        log.warn("queueAudioConnect was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId, new RuntimeException());
    }

}
