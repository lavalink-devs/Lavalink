package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AudioLoader implements AudioLoadResultHandler {

    private static final Logger log = LoggerFactory.getLogger(AudioLoader.class);

    private List<AudioTrack> loadedItems;
    private boolean used = false;

    List<AudioTrack> loadSync(String identifier) throws InterruptedException {
        if(used)
            throw new IllegalStateException("This loader can only be used once per instance");

        used = true;

        Player.PLAYER_MANAGER.loadItem(identifier, this);

        synchronized (this) {
            this.wait();
        }

        return loadedItems;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        loadedItems = new ArrayList<>();
        loadedItems.add(audioTrack);
        log.info("Loaded track " + audioTrack.getInfo().title);
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        log.info("Loaded playlist " + audioPlaylist.getName());
        loadedItems = audioPlaylist.getTracks();
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void noMatches() {
        log.info("No matches found");
        loadedItems = new ArrayList<>();
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void loadFailed(FriendlyException e) {
        log.error("Load failed", e);
        loadedItems = new ArrayList<>();
        synchronized (this) {
            this.notify();
        }
    }

}
