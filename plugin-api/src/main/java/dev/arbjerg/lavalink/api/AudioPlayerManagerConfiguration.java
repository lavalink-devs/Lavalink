package dev.arbjerg.lavalink.api;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * Allows modification of Lavalink's {@link AudioPlayerManager}
 * <p>
 * Note: Beans of type {@link com.sedmelluq.discord.lavaplayer.source.AudioSourceManager} are automatically loaded by
 * Lavalink, in which case using this interface is redundant.
 */
public interface AudioPlayerManagerConfiguration {
    AudioPlayerManager configure(AudioPlayerManager manager);
}
