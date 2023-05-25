package dev.arbjerg.lavalink.api

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager

/**
 * Allows modification of Lavalink's [AudioPlayerManager]
 *
 *
 * Note: Beans of type [AudioSourceManager] are automatically loaded by
 * Lavalink, in which case using this interface is redundant.
 */
interface AudioPlayerManagerConfiguration {
    /**
     * Creates a new custom configured [AudioPlayerManager] based on [manager].
     */
    fun configure(manager: AudioPlayerManager): AudioPlayerManager
}
