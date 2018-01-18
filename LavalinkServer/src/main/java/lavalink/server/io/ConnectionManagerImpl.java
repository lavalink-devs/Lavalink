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
        throw new RuntimeException("queueAudioConnect was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId);
    }

    @Override
    public void onDisconnect(String guildId) {
        throw new RuntimeException("onDisconnect was requested, this shouldn't happen, guildId:" + guildId);
    }

}
