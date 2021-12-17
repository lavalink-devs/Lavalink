package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * Allows modification of Lavalink's {@link AudioPlayerManager}
 */
public interface AudioPlayerManagerConfiguration {
    AudioPlayerManager configure(AudioPlayerManager manager);
}
