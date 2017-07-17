package lavalink.server.util;

import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.server.player.Player;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Util {

    public static int getShardFromSnowflake(String snowflake, int numShards) {
        return (int) ((Long.parseLong(snowflake) >> 22) % numShards);
    }

    public static AudioTrack toAudioTrack(String message) throws IOException {
        byte[] b64 = Base64.decodeBase64(message);
        ByteArrayInputStream bais = new ByteArrayInputStream(b64);
        return Player.PLAYER_MANAGER.decodeTrack(new MessageInput(bais)).decodedTrack;
    }

    public static String toMessage(AudioTrack track) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Player.PLAYER_MANAGER.encodeTrack(new MessageOutput(baos), track);
        return Base64.encodeBase64String(baos.toByteArray());
    }

}
