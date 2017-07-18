package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lavalink.server.Config;
import lavalink.server.Launcher;
import lavalink.server.io.SocketContext;
import net.dv8tion.jda.audio.AudioSendHandler;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Player implements AudioSendHandler {

    private static final Logger log = LoggerFactory.getLogger(Player.class);

    public static final AudioPlayerManager PLAYER_MANAGER;

    static {
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        PLAYER_MANAGER.enableGcMonitoring();

        Config.Sources sources = Launcher.config.getSources();
        if (sources.isYoutube()) PLAYER_MANAGER.registerSourceManager(new YoutubeAudioSourceManager());
        if (sources.isBandcamp()) PLAYER_MANAGER.registerSourceManager(new BandcampAudioSourceManager());
        if (sources.isSoundcloud()) PLAYER_MANAGER.registerSourceManager(new SoundCloudAudioSourceManager());
        if (sources.isTwitch()) PLAYER_MANAGER.registerSourceManager(new TwitchStreamAudioSourceManager());
        if (sources.isVimeo()) PLAYER_MANAGER.registerSourceManager(new VimeoAudioSourceManager());
        if (sources.isHttp()) PLAYER_MANAGER.registerSourceManager(new HttpAudioSourceManager());
    }

    private final SocketContext socketContext;
    private final String guildId;
    private final AudioPlayer player;
    private AudioLossCounter audioLossCounter = new AudioLossCounter();
    private AudioFrame lastFrame = null;

    public Player(SocketContext socketContext, String guildId) {
        this.socketContext = socketContext;
        this.guildId = guildId;
        this.player = PLAYER_MANAGER.createPlayer();
        this.player.addListener(new EventEmitter(this));
        this.player.addListener(audioLossCounter);
    }

    public void play(AudioTrack track) {
        player.playTrack(track);
    }

    public void stop() {
        player.stopTrack();
    }

    public void setPause(boolean b) {
        player.setPaused(b);
    }

    public String getGuildId() {
        return guildId;
    }

    public void seekTo(long position) {
        player.getPlayingTrack().setPosition(position);
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }

    public JSONObject getState() {
        JSONObject json = new JSONObject();

        json.put("position", player.getPlayingTrack().getPosition());
        json.put("time", System.currentTimeMillis());

        return json;
    }

    SocketContext getSocket() {
        return socketContext;
    }

    @Override
    public boolean canProvide() {
        lastFrame = player.provide();

        if(lastFrame == null) {
            audioLossCounter.onLoss();
            return false;
        } else {
            audioLossCounter.onSuccess();
            return true;
        }
    }

    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    public AudioLossCounter getAudioLossCounter() {
        return audioLossCounter;
    }

    public boolean isPlaying() {
        return player.getPlayingTrack() != null && !player.isPaused();
    }

}
