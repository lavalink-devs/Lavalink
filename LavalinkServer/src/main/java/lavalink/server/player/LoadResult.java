package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

class LoadResult {
    public ResultStatus loadResultType;
    public List<AudioTrack> tracks;
    public String playlistName;
    public Integer selectedTrack;
    public FriendlyException exception;

    public LoadResult(ResultStatus loadResultType, List<AudioTrack> tracks,
                      @Nullable String playlistName, @Nullable Integer selectedTrack,
                      @Nullable FriendlyException exception) {
        // Exception is only provided when loadResultType is LOAD_FAILED.

        this.loadResultType = loadResultType;
        this.tracks = Collections.unmodifiableList(tracks);
        this.playlistName = playlistName;
        this.selectedTrack = selectedTrack;
        this.exception = exception;
    }
}
