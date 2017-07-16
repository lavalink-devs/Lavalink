package lavalink.client;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.java_websocket.drafts.Draft_6455;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.function.Function;

public class Lavalink {

    private final LavalinkSocket socket;
    private final int numShards;
    private final Function<Integer, JDA> jdaProvider;

    public Lavalink(URI serverUri, String password, int numShards, Function<Integer, JDA> jdaProvider) {
        this.numShards = numShards;
        this.jdaProvider = jdaProvider;

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", password);
        headers.put("Num-Shards", Integer.toString(numShards));
        socket = new LavalinkSocket(this, serverUri, new Draft_6455(), headers);
    }

    public void openVoiceConnection(VoiceChannel channel) {
        JSONObject json = new JSONObject();
        json.put("op", "connect");
        json.put("guildId", channel.getGuild().getId());
        json.put("channelId", channel.getId());
        socket.send(json.toString());
    }

    public void interceptJdaAudio(JDAImpl jda) {
        jda.getClient().getHandlers().put("VOICE_SERVER_UPDATE", new VoiceServerUpdateInterceptor(this, jda));
    }

    public void shutdown() {
        socket.close();
    }

    LavalinkSocket getSocket() {
        return socket;
    }

    JDA getShard(int num) {
        return jdaProvider.apply(num);
    }
}
