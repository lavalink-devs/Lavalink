package lavalink.server.player;

public class AudioLossCounter {

    public static final int EXPECTED_PACKET_COUNT_PER_MIN = (60 * 1000) / 20; // 20ms packets

    private long curMinute = 0;
    private int curLoss = 0;
    private int curSucc = 0;

    private int lastLoss = 0;
    private int lastSucc = 0;

    AudioLossCounter() {
    }

    void onLoss() {
        checkTime();
        curLoss++;
    }

    void onSuccess() {
        checkTime();
        curSucc++;
    }

    public int getLastMinuteLoss() {
        return lastLoss;
    }

    public int getLastMinuteSuccess() {
        return lastSucc;
    }

    private void checkTime() {
        long actualMinute = System.currentTimeMillis() / 60000;

        if(curMinute != actualMinute) {
            lastLoss = curLoss;
            lastSucc = curSucc;
            curLoss = 0;
            curSucc = 0;
            curMinute = actualMinute;
        }
    }

    @Override
    public String toString() {
        return "AudioLossCounter{" +
                "lastLoss=" + lastLoss +
                ", lastSucc=" + lastSucc +
                ", total=" + (lastSucc + lastLoss) +
                '}';
    }
}
