package lavalink.client.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;

public class PlayerState {

    private AudioTrack track = null;
    private boolean isPaused = false;
    private float volume = 1f;
    private long updateTime = -1;
    private long position = -1;

    private void provideState(JSONObject json) {

    }

}
