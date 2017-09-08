package lavalink.server.player;

import com.sedmelluq.discord.lavaplayer.container.MediaContainer;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerProbe;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;

public class EncodableLocalAudioSourceManager extends LocalAudioSourceManager {
    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        LocalAudioTrack localAudioTrack = (LocalAudioTrack) track;

        try {
            Field probeField = LocalAudioTrack.class.getDeclaredField("probe");
            probeField.setAccessible(true);

            MediaContainerProbe mediaContainerProbe = (MediaContainerProbe) probeField.get(localAudioTrack);
            output.writeUTF(mediaContainerProbe.getName());
        } catch (IllegalAccessException | IOException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        String probeName;
        try {
            probeName = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        for (MediaContainer container : MediaContainer.class.getEnumConstants()) {
            if (container.probe.getName().equals(probeName)) {
                return new LocalAudioTrack(trackInfo, container.probe, this);
            }
        }

        return null;
    }
}