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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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

class LavalinkTest {

    private static final Logger log = LoggerFactory.getLogger(LavalinkTest.class);

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
    static void setUp() {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(System.getenv("TEST_TOKEN"))
                    .addEventListener(lavalink)
                    .buildBlocking();

            lavalink = new Lavalink("152691313123393536", 1, integer -> jda);
            lavalink.addNode(new URI("ws://localhost"), "youshallnotpass");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        lavalink.shutdown();
        jda.shutdown();
    }

    @Test
    void vcJoinTest() {
        VoiceChannel vc = jda.getVoiceChannelById(System.getenv("TEST_VOICE_CHANNEL"));
        lavalink.getLink(vc.getGuild()).connect(vc);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        lavalink.getLink(vc.getGuild()).disconnect();
    }

    private List<AudioTrack> loadAudioTracks(String identifier) {
        try {
            JSONArray trackData = Unirest.get("http://localhost:2333/loadtracks?identifier=" + URLEncoder.encode(identifier, "UTF-8"))
                    .header("Authorization", "youshallnotpass")
                    .asJson()
                    .getBody()
                    .getObject()
                    .getJSONArray("tracks");

            ArrayList<AudioTrack> list = new ArrayList<>();
            trackData.forEach(o -> {
                try {
                    list.add(LavalinkUtil.toAudioTrack((String) o));
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
        VoiceChannel vc = jda.getVoiceChannelById(System.getenv("TEST_VOICE_CHANNEL"));
        lavalink.getLink(vc.getGuild()).connect(vc);

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
        lavalink.getLink(vc.getGuild()).disconnect();
        player.removeListener(listener);
        player.stopTrack();

        Assertions.assertEquals(0, latch.getCount());
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
        VoiceChannel vc = jda.getVoiceChannelById(System.getenv("TEST_VOICE_CHANNEL"));
        lavalink.getLink(vc.getGuild()).connect(vc);

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
        lavalink.getLink(vc.getGuild()).disconnect();
        player.removeListener(listener);
        player.stopTrack();

        Assertions.assertEquals(0, latch.getCount());
    }

    @Test
    void testPlayback() throws InterruptedException {
        VoiceChannel vc = jda.getVoiceChannelById(System.getenv("TEST_VOICE_CHANNEL"));
        Link link = lavalink.getLink(vc.getGuild());
        link.connect(vc);

        IPlayer player = link.getPlayer();
        CountDownLatch latch = new CountDownLatch(1);

        PlayerEventListenerAdapter listener = new PlayerEventListenerAdapter() {
            @Override
            public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason == AudioTrackEndReason.FINISHED) {
                    latch.countDown();
                }
            }
        };

        player.addListener(listener);

        String jingle = BILL_WURTZ_JINGLES[(int) (Math.random() * BILL_WURTZ_JINGLES.length)];

        player.playTrack(loadAudioTracks(jingle).get(0));

        latch.await(20, TimeUnit.SECONDS);
        link.disconnect();
        player.removeListener(listener);

        player.stopTrack();

        Assertions.assertEquals(0, latch.getCount());
    }

}
