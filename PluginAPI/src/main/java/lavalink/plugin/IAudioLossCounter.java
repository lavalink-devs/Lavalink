package lavalink.plugin;

public interface IAudioLossCounter {
    int getLastMinuteLoss();
    int getLastMinuteSuccess();
    boolean isDataUsable();
}
