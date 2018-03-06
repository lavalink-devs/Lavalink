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

package lavalink.client;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.Lavalink;
import lavalink.client.io.Link;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequireSystemProperty({
        LavalinkTest.PROPERTY_TOKEN,
        LavalinkTest.PROPERTY_CHANNEL,
})
class LavalinkTest {

    private static final Logger log = LoggerFactory.getLogger(LavalinkTest.class);

    public static final String PROPERTY_TOKEN = "TEST_TOKEN";
    public static final String PROPERTY_CHANNEL = "TEST_VOICE_CHANNEL";

    private static JDA jda = null;
    private static Lavalink lavalink = null;
    private static final String[] BILL_WURTZ_JINGLES = {
            "https://www.youtube.com/watch?v=GtwVQbUSasw",
            "https://www.youtube.com/watch?v=eNxMkZcySKs",
            "https://www.youtube.com/watch?v=4q1Zs3vbX8M",
            "https://www.youtube.com/watch?v=sqPTS16mi9M",
            "https://www.youtube.com/watch?v=dWqPb16Ox-0",
            "https://www.youtube.com/watch?v=mxyPtMON4IM",
            "https://www.youtube.com/watch?v=DLutlHlw4C0"
    };

    @BeforeAll
    static void setUp() throws Exception {
        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
                .setToken(getSystemProperty(PROPERTY_TOKEN));

        JDA selfId = jdaBuilder.buildAsync();
        lavalink = new Lavalink(selfId.asBot().getApplicationInfo().submit().get(30, TimeUnit.SECONDS).getId(), 1, integer -> jda);
        selfId.shutdown();

        lavalink.addNode(new URI("ws://localhost:5555"), "youshallnotpass");

        jda = jdaBuilder
                .addEventListener(lavalink)
                .buildAsync();

        Thread.sleep(2000);
        assertTrue(lavalink.getNodes().get(0).isAvailable(), "Could not connect to lavalink server");
    }

    @AfterAll
    static void tearDown() {
        if (lavalink != null) {
            lavalink.shutdown();
        }
        if (jda != null) {
            jda.shutdown();
        }
    }

    @Test
    void vcJoinTest() {
        VoiceChannel vc = fetchVoiceChannel(jda, getTestVoiceChannelId());
        ensureConnected(lavalink, vc);
        assertEquals(Link.State.CONNECTED, lavalink.getLink(vc.getGuild()).getState(), "Failed to connect to voice channel");
        ensureNotConnected(lavalink, vc);
    }

    private List<AudioTrack> loadAudioTracks(String identifier) {
        try {
            JSONArray trackData = Unirest.get("http://localhost:2333/loadtracks?identifier=" + URLEncoder.encode(identifier, "UTF-8"))
                    .header("Authorization", "youshallnotpass")
                    .asJson()
                    .getBody()
                    .getArray();

            ArrayList<AudioTrack> list = new ArrayList<>();
            trackData.forEach(o -> {
                try {
                    list.add(LavalinkUtil.toAudioTrack(((JSONObject) o).getString("track")));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            return list;
        } catch (UnirestException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void connectAndPlay(AudioTrack track) throws InterruptedException {
        VoiceChannel vc = fetchVoiceChannel(jda, getTestVoiceChannelId());
        ensureConnected(lavalink, vc);

        IPlayer player = lavalink.getLink(vc.getGuild()).getPlayer();
        CountDownLatch latch = new CountDownLatch(1);
        PlayerEventListenerAdapter listener = new PlayerEventListenerAdapter() {
            @Override
            public void onTrackStart(IPlayer player, AudioTrack track) {
                latch.countDown();
            }
        };
        player.addListener(listener);

        player.playTrack(track);

        latch.await(5, TimeUnit.SECONDS);
        ensureNotConnected(lavalink, vc);
        player.removeListener(listener);
        player.stopTrack();

        assertEquals(0, latch.getCount());
    }

    @Test
    void vcPlayTest() throws InterruptedException {
        connectAndPlay(loadAudioTracks("aGOFOP2BIhI").get(0));
    }

    @Test
    void vcStreamTest() throws InterruptedException {
        connectAndPlay(loadAudioTracks("https://gensokyoradio.net/GensokyoRadio.m3u").get(0));
    }

    @Test
    void stopTest() throws InterruptedException {
        VoiceChannel vc = fetchVoiceChannel(jda, getTestVoiceChannelId());
        ensureConnected(lavalink, vc);

        IPlayer player = lavalink.getLink(vc.getGuild()).getPlayer();
        CountDownLatch latch = new CountDownLatch(1);

        PlayerEventListenerAdapter listener = new PlayerEventListenerAdapter() {
            @Override
            public void onTrackStart(IPlayer player, AudioTrack track) {
                player.stopTrack();
            }

            @Override
            public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason == AudioTrackEndReason.STOPPED) {
                    latch.countDown();
                }
            }
        };

        player.addListener(listener);

        player.playTrack(loadAudioTracks("aGOFOP2BIhI").get(0));

        latch.await(5, TimeUnit.SECONDS);
        ensureNotConnected(lavalink, vc);
        player.removeListener(listener);
        player.stopTrack();

        assertEquals(0, latch.getCount());
    }

    @Test
    void testPlayback() throws InterruptedException {
        VoiceChannel vc = fetchVoiceChannel(jda, getTestVoiceChannelId());
        Link link = lavalink.getLink(vc.getGuild());
        ensureConnected(lavalink, vc);

        IPlayer player = link.getPlayer();
        CountDownLatch latch = new CountDownLatch(1);

        PlayerEventListenerAdapter listener = new PlayerEventListenerAdapter() {
            @Override
            public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                log.info(endReason.name());
                if (endReason == AudioTrackEndReason.FINISHED) {
                    latch.countDown();
                }
            }
        };

        player.addListener(listener);

        String jingle = BILL_WURTZ_JINGLES[(int) (Math.random() * BILL_WURTZ_JINGLES.length)];

        player.playTrack(loadAudioTracks(jingle).get(0));

        latch.await(20, TimeUnit.SECONDS);
        ensureNotConnected(lavalink, vc);
        player.removeListener(listener);

        player.stopTrack();

        assertEquals(0, latch.getCount());
    }

    private static String getSystemProperty(String key) {
        String value = System.getProperty(key);

        assertNotNull(value, "Missing system property " + key);
        assertFalse(value.isEmpty(), "System property " + key + " is empty");

        return value;
    }

    private static long getTestVoiceChannelId() {
        return Long.parseUnsignedLong(getSystemProperty(PROPERTY_CHANNEL));
    }

    private static VoiceChannel fetchVoiceChannel(JDA jda, long voiceChannelId) {
        long started = System.currentTimeMillis();
        while (jda.getStatus() != JDA.Status.CONNECTED
                && System.currentTimeMillis() - started < 10000 //wait 10 sec max
                && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(JDA.Status.CONNECTED, jda.getStatus(), "Failed to connect to Discord in a reasonable amount of time");

        VoiceChannel voiceChannel = jda.getVoiceChannelById(voiceChannelId);
        assertNotNull(voiceChannel, "Configured VoiceChannel not found on the configured Discord bot account");

        return voiceChannel;
    }


    private static void ensureConnected(Lavalink lavalink, VoiceChannel voiceChannel) {

        Link link = lavalink.getLink(voiceChannel.getGuild());
        link.connect(voiceChannel);
        long started = System.currentTimeMillis();
        while (link.getState() != Link.State.CONNECTED
                && System.currentTimeMillis() - started < 10000 //wait 10 sec max
                && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            link.connect(voiceChannel);
        }

        assertEquals(Link.State.CONNECTED, link.getState(), "Failed to connect to voice channel in a reasonable amount of time");
    }

    private static void ensureNotConnected(Lavalink lavalink, VoiceChannel voiceChannel) {
        Link link = lavalink.getLink(voiceChannel.getGuild());
        link.disconnect();
        long started = System.currentTimeMillis();
        while (link.getState() != Link.State.NOT_CONNECTED && link.getState() != Link.State.DISCONNECTING
                && System.currentTimeMillis() - started < 10000 //wait 10 sec max
                && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            link.disconnect();
        }

        assertTrue(link.getState() == Link.State.NOT_CONNECTED
                || link.getState() == Link.State.DISCONNECTING, "Failed to disconnect from voice channel in a reasonable amount of time");
    }
}
