package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.List;

class LoadResult {
    public List<AudioTrack> tracks;
    public boolean isPlaylist;
    public String playlistName;
    public ResultStatus loadResultType;
    public Integer selectedTrack;

    LoadResult(List<AudioTrack> tracks, boolean isPlaylist, String playlistName, ResultStatus loadResultType, Integer selectedTrack) {
        this.tracks = Collections.unmodifiableList(tracks);
        this.isPlaylist = isPlaylist;
        this.playlistName = playlistName;
        this.loadResultType = loadResultType;
        this.selectedTrack = selectedTrack;
    }
}
