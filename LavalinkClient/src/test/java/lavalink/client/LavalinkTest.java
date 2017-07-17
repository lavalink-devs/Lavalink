package lavalink.client;

import lavalink.client.io.Lavalink;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

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
        log.info("Connecting to " + vc);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Disconnecting from " + vc);
        lavalink.closeVoiceConnection(vc);

        Assertions.assertTrue(true);
    }

}
