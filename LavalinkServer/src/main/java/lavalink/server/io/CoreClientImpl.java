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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.dv8tion.jda.CoreClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CoreClientImpl implements CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreClientImpl.class);
    private static final int TIMEOUT = 60 * 1000;

    private final WebSocket socket;
    private int shardId;

    private final Object validationObj = new Object();
    private final Object isConnectionObj = new Object();
    private boolean connected = false;

    private LoadingCache<String, Boolean> guildValidMap = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<String, Boolean>() {
                        @Override
                        public Boolean load(@SuppressWarnings("NullableProblems") String guild) throws Exception {
                            return requestValidationSync(guild, null);
                        }
                    }
            );

    private LoadingCache<ImmutablePair<String, String>, Boolean> channelValidMap = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<ImmutablePair<String, String>, Boolean>() {
                        @Override
                        public Boolean load(@SuppressWarnings("NullableProblems") ImmutablePair<String, String> key) throws Exception {
                            return requestValidationSync(key.left, key.right);
                        }
                    }
            );


    CoreClientImpl(WebSocket socket, int shardId) {
        this.socket = socket;
        this.shardId = shardId;
    }

    @Override
    public void sendWS(String message) {
        log.info(message);
        JSONObject json = new JSONObject();
        json.put("op", "sendWS");
        json.put("shardId", shardId);
        json.put("message", message);
        socket.send(json.toString());
    }

    @Override
    public boolean isConnected() {
        return requestIsConnectedSync();
    }

    @Override
    public boolean inGuild(String guildId) {
        log.info("Requested guild check");
        boolean val = guildValidMap.getUnchecked(guildId);
        if (!val) {
            log.warn("Requested guild check but validation was false!");
        }
        return val;
    }

    @Override
    public boolean voiceChannelExists(String guildId, String channelId) {
        log.info("Requested channel check");
        boolean val = channelValidMap.getUnchecked(new ImmutablePair<>(guildId, channelId));
        if (!val) {
            log.warn("Requested channel check but validation was false!");
        }
        return val;
    }

    @Override
    public boolean hasPermissionInChannel(String guildId, String channelId, long l) {
        log.info("Requested permission check");
        boolean val = channelValidMap.getUnchecked(new ImmutablePair<>(guildId, channelId));
        if (!val) {
            log.warn("Requested permission check but validation was false!");
        }
        return val;
    }



    private boolean requestValidationSync(String guildId, String channelId) {
        JSONObject json = new JSONObject();
        json.put("op", "validationReq");
        json.put("guildId", guildId);

        if (channelId != null) {
            json.put("channelId", channelId);
        }

        long startTime = System.currentTimeMillis();
        socket.send(json.toString());

        try {
            synchronized (validationObj) {
                validationObj.wait(TIMEOUT);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (System.currentTimeMillis() - startTime >= TIMEOUT) {
            log.error("Validation timed out after " + TIMEOUT + " millis");
            return false;
        }

        return channelId == null
                ? guildValidMap.getIfPresent(guildId) == Boolean.TRUE
                : channelValidMap.getIfPresent(new ImmutablePair<>(guildId, channelId)) == Boolean.TRUE;
    }

    void provideValidation(String guildId, String channelId, boolean valid) {
        guildValidMap.put(guildId, valid);
        if (channelId != null)
            channelValidMap.put(new ImmutablePair<>(guildId, channelId), valid);

        synchronized (validationObj) {
            validationObj.notifyAll();
        }
    }

    private boolean requestIsConnectedSync() {
        JSONObject json = new JSONObject();
        json.put("op", "isConnectedReq");
        json.put("shardId", shardId);

        long startTime = System.currentTimeMillis();
        socket.send(json.toString());

        try {
            synchronized (isConnectionObj) {
                isConnectionObj.wait(TIMEOUT);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (System.currentTimeMillis() - startTime >= TIMEOUT) {
            throw new RuntimeException("Connection checking timed out after " + TIMEOUT + " millis");
        }

        return connected;
    }

    void provideIsConnected(boolean connected) {
        this.connected = connected;
        synchronized (isConnectionObj) {
            isConnectionObj.notifyAll();
        }
    }

}
