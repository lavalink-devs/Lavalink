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

import com.github.shredder121.asyncaudio.jdaaudio.AsyncPacketProviderFactory;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.server.config.AudioSendFactoryConfiguration;
import lavalink.server.config.ServerConfig;
import lavalink.server.player.Player;
import lavalink.server.util.Util;
import net.dv8tion.jda.Core;
import net.dv8tion.jda.audio.factory.IAudioSendFactory;
import net.dv8tion.jda.manager.AudioManager;
import net.dv8tion.jda.manager.ConnectionManagerBuilder;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SocketContext {

    private static final Logger log = LoggerFactory.getLogger(SocketContext.class);

    private final AudioPlayerManager audioPlayerManager;
    private final ServerConfig serverConfig;
    private final WebSocket socket;
    private final AudioSendFactoryConfiguration audioSendFactoryConfiguration;
    private String userId;
    private int shardCount;
    private final Map<Integer, Core> cores = new HashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private ScheduledExecutorService statsExecutor;
    public final ScheduledExecutorService playerUpdateService;
    private final ConcurrentHashMap<Integer, IAudioSendFactory> sendFactories = new ConcurrentHashMap<>();

    SocketContext(Supplier<AudioPlayerManager> audioPlayerManagerSupplier, ServerConfig serverConfig, WebSocket socket,
                  AudioSendFactoryConfiguration audioSendFactoryConfiguration, SocketServer socketServer,
                  String userId, int shardCount) {
        this.audioPlayerManager = audioPlayerManagerSupplier.get();
        this.serverConfig = serverConfig;
        this.socket = socket;
        this.audioSendFactoryConfiguration = audioSendFactoryConfiguration;
        this.userId = userId;
        this.shardCount = shardCount;

        statsExecutor = Executors.newSingleThreadScheduledExecutor();
        statsExecutor.scheduleAtFixedRate(new StatsTask(this, socketServer), 0, 1, TimeUnit.MINUTES);

        playerUpdateService = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("player-update");
            thread.setDaemon(true);
            return thread;
        });
    }

    Core getCore(int shardId) {
        return cores.computeIfAbsent(shardId,
                __ -> {
                    if (audioSendFactoryConfiguration.isNasSupported())
                        return new Core(userId, new CoreClientImpl(), core -> new ConnectionManagerImpl(), getAudioSendFactory(shardId));
                    else
                        return new Core(userId, new CoreClientImpl(), (ConnectionManagerBuilder) core -> new ConnectionManagerImpl());
                }
        );
    }

    Player getPlayer(String guildId) {
        return players.computeIfAbsent(guildId,
                __ -> new Player(this, guildId, audioPlayerManager)
        );
    }

    int getShardCount() {
        return shardCount;
    }

    public WebSocket getSocket() {
        return socket;
    }

    Map<String, Player> getPlayers() {
        return players;
    }

    List<Player> getPlayingPlayers() {
        List<Player> newList = new LinkedList<>();
        players.values().forEach(player -> {
            if (player.isPlaying()) newList.add(player);
        });
        return newList;
    }

    void shutdown() {
        log.info("Shutting down " + cores.size() + " cores and " + getPlayingPlayers().size() + " playing players.");
        statsExecutor.shutdown();
        audioPlayerManager.shutdown();
        playerUpdateService.shutdown();
        players.keySet().forEach(s -> {
            Core core = cores.get(Util.getShardFromSnowflake(s, shardCount));
            if (core != null) {
                AudioManager audioManager = core.getAudioManager(s);
                if (audioManager != null) {
                    audioManager.closeAudioConnection();
                }
            }
        });

        players.values().forEach(Player::stop);
    }

    private IAudioSendFactory getAudioSendFactory(int shardId) {
        return sendFactories.computeIfAbsent(shardId % audioSendFactoryConfiguration.getAudioSendFactoryCount(),
                integer -> {
                    Integer customBuffer = serverConfig.getBufferDurationMs();
                    NativeAudioSendFactory nativeAudioSendFactory;
                    if (customBuffer != null) {
                        nativeAudioSendFactory = new NativeAudioSendFactory(customBuffer);
                    } else {
                        nativeAudioSendFactory = new NativeAudioSendFactory();
                    }

                    return AsyncPacketProviderFactory.adapt(nativeAudioSendFactory);
                });
    }

}
