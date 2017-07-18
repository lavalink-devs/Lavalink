package lavalink.client.io;

import lavalink.client.player.IPlayer;
import lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Lavalink {

    private final int numShards;
    private final Function<Integer, JDA> jdaProvider;
    private final HashMap<String, String> connectedChannels = new HashMap<>(); // Key is guild id
    private final HashMap<String, LavalinkPlayer> players = new HashMap<>(); // Key is guild id
    private final List<LavalinkSocket> nodes = new ArrayList<>();
    private final LavalinkLoadBalancer loadBalancer = new LavalinkLoadBalancer(this);

    public Lavalink(int numShards, Function<Integer, JDA> jdaProvider) {
        this.numShards = numShards;
        this.jdaProvider = jdaProvider;
    }

    public void addNode(URI serverUri, String password) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", password);
        headers.put("Num-Shards", Integer.toString(numShards));
        nodes.add(new LavalinkSocket(this, serverUri, new Draft_6455(), headers));
    }

    public void openVoiceConnection(VoiceChannel channel) {
        JSONObject json = new JSONObject();
        json.put("op", "connect");
        json.put("guildId", channel.getGuild().getId());
        json.put("channelId", channel.getId());
        loadBalancer.getSocket(channel.getGuild()).send(json.toString());
        connectedChannels.put(channel.getGuild().getId(), channel.getId());
    }

    public void closeVoiceConnection(VoiceChannel channel) {
        JSONObject json = new JSONObject();
        json.put("op", "disconnect");
        json.put("guildId", channel.getGuild().getId());
        json.put("channelId", channel.getId());
        loadBalancer.getSocket(channel.getGuild()).send(json.toString());
        connectedChannels.remove(channel.getGuild().getId());
    }

    public void interceptJdaAudio(JDA jda) {
        ((JDAImpl) jda).getClient().getHandlers().put("VOICE_SERVER_UPDATE", new VoiceServerUpdateInterceptor(this, (JDAImpl) jda));
    }

    public IPlayer getPlayer(String guildId) {
        return players.computeIfAbsent(guildId, __ -> new LavalinkPlayer(loadBalancer.getSocket(guildId), guildId));
    }

    public void shutdown() {
        nodes.forEach(WebSocketClient::close);
    }

    LavalinkSocket getSocket(String guildId) {
        return loadBalancer.getSocket(guildId);
    }

    JDA getShard(int num) {
        return jdaProvider.apply(num);
    }

    public int getNumShards() {
        return numShards;
    }

    public List<LavalinkSocket> getNodes() {
        return nodes;
    }
}
