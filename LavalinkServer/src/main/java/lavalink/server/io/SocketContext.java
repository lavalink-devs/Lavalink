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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import lavalink.server.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import space.npstr.magma.MagmaApi;
import space.npstr.magma.MagmaMember;
import space.npstr.magma.Member;

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
    private final WebSocketSession socket;
    private String userId;
    private final MagmaApi magmaApi;
    //guildId <-> Player
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private ScheduledExecutorService statsExecutor;
    public final ScheduledExecutorService playerUpdateService;

    SocketContext(Supplier<AudioPlayerManager> audioPlayerManagerSupplier, WebSocketSession socket, SocketServer socketServer, String userId,
                  MagmaApi magmaApi) {
        this.audioPlayerManager = audioPlayerManagerSupplier.get();
        this.socket = socket;
        this.userId = userId;
        this.magmaApi = magmaApi;

        statsExecutor = Executors.newSingleThreadScheduledExecutor();
        statsExecutor.scheduleAtFixedRate(new StatsTask(this, socketServer), 0, 1, TimeUnit.MINUTES);

        playerUpdateService = Executors.newScheduledThreadPool(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("player-update");
            thread.setDaemon(true);
            return thread;
        });
    }

    public String getUserId() {
        return userId;
    }

    Player getPlayer(String guildId) {
        return players.computeIfAbsent(guildId,
                __ -> new Player(this, guildId, audioPlayerManager)
        );
    }

    public WebSocketSession getSession() {
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
        log.info("Shutting down " + getPlayingPlayers().size() + " playing players.");
        statsExecutor.shutdown();
        audioPlayerManager.shutdown();
        playerUpdateService.shutdown();
        players.keySet().forEach(guildId -> {
            Member member = MagmaMember.builder()
                    .userId(userId)
                    .guildId(guildId)
                    .build();
            magmaApi.removeSendHandler(member);
            magmaApi.closeConnection(member);
        });

        players.values().forEach(Player::stop);
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }
}
