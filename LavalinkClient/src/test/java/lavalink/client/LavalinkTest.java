package lavalink.client;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.io.Lavalink;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.PlayerEventListenerAdapter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.SimpleLog;
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

    @BeforeAll
    static void setUp() {
        //Attach log adapter
        SimpleLog.addListener(new SimpleLogToSLF4JAdapter());

        //Make JDA not print to console, we have Logback for that
        SimpleLog.LEVEL = SimpleLog.Level.OFF;

        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(System.getenv("TEST_TOKEN"))
                    .buildBlocking();

            lavalink = new Lavalink(new URI("ws://localhost"), "youshallnotpass", 1, integer -> jda);
            lavalink.interceptJdaAudio(jda);
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
        lavalink.openVoiceConnection(vc);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        lavalink.closeVoiceConnection(vc);
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
        lavalink.openVoiceConnection(vc);

        IPlayer player = lavalink.getPlayer(vc.getGuild().getId());
        CountDownLatch latch = new CountDownLatch(1);
        player.addListener(new PlayerEventListenerAdapter() {
            @Override
            public void onTrackStart(IPlayer player, AudioTrack track) {
                latch.countDown();
            }
        });

        player.playTrack(track);

        latch.await(5, TimeUnit.SECONDS);
        lavalink.closeVoiceConnection(vc);

        Assertions.assertEquals(latch.getCount(), 0);
    }

    @Test
    void vcPlayTest() throws InterruptedException {
        connectAndPlay(loadAudioTracks("aGOFOP2BIhI").get(0));
    }

    @Test
    void vcStreamTest() throws InterruptedException {
        connectAndPlay(loadAudioTracks("https://www.youtube.com/watch?v=MWZiKbWcVVQ").get(0));
    }

    @Test
    void stopTest() throws InterruptedException {
        VoiceChannel vc = jda.getVoiceChannelById(System.getenv("TEST_VOICE_CHANNEL"));
        lavalink.openVoiceConnection(vc);

        IPlayer player = lavalink.getPlayer(vc.getGuild().getId());
        CountDownLatch latch = new CountDownLatch(1);

        player.addListener(new PlayerEventListenerAdapter() {
            @Override
            public void onTrackEnd(IPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                if (endReason == AudioTrackEndReason.STOPPED) {
                    latch.countDown();
                }
            }
        });

        player.playTrack(loadAudioTracks("aGOFOP2BIhI").get(0));
        player.stop();

        latch.await(5, TimeUnit.SECONDS);
        lavalink.closeVoiceConnection(vc);

        Assertions.assertEquals(latch.getCount(), 0);
    }

}
