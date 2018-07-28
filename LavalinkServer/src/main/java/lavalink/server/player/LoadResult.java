package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

class LoadResult {
    public List<AudioTrack> tracks;
    public String playlistName;
    public ResultStatus loadResultType;
    public Integer selectedTrack;

    public LoadResult(List<AudioTrack> tracks, @Nullable String playlistName, ResultStatus loadResultType,
                      @Nullable Integer selectedTrack) {

        this.tracks = Collections.unmodifiableList(tracks);
        this.playlistName = playlistName;
        this.loadResultType = loadResultType;
        this.selectedTrack = selectedTrack;
    }
}
