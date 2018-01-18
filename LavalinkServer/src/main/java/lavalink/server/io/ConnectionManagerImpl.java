package lavalink.server.io;

import net.dv8tion.jda.manager.ConnectionManager;

/**
 * Created by Repulser
 * https://github.com/Repulser
 */
public class ConnectionManagerImpl implements ConnectionManager {

    @Override
    public void removeAudioConnection(String guildId) {
    }

    @Override
    public void queueAudioConnect(String guildId, String channelId) {
        new RuntimeException("queueAudioConnect was requested, this shouldn't happen, guildId:" + guildId + " channelId:" + channelId).printStackTrace();
    }

    @Override
    public void onDisconnect(String guildId) {
        new RuntimeException("onDisconnect was requested, this shouldn't happen, guildId:" + guildId).printStackTrace();
    }

}
