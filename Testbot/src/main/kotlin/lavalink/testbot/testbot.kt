package lavalink.testbot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.io.jda.JdaLavalink
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI

private val log: Logger = LoggerFactory.getLogger("Testbot")
private lateinit var jda: JDA
private val lavalink = JdaLavalink(1) { _ -> jda }
private val playerManager = DefaultAudioPlayerManager()

fun main() {
    AudioSourceManagers.registerRemoteSources(playerManager)

    jda = JDABuilder.createDefault(System.getenv("TOKEN"),
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_VOICE_STATES)
            .disableCache(CLIENT_STATUS, ACTIVITY, CLIENT_STATUS, EMOTE, MEMBER_OVERRIDES)
            .addEventListeners(Listener, lavalink)
            .setVoiceDispatchInterceptor(lavalink.voiceInterceptor)
            .build()
}

object Listener : ListenerAdapter() {
    override fun onStatusChange(event: StatusChangeEvent) {
        log.info("{} -> {}", event.oldStatus, event.newStatus)
    }

    override fun onReady(event: ReadyEvent) {
        lavalink.setUserId(jda.selfUser.id)
        lavalink.addNode(URI("ws://localhost:2333"), "youshallnotpass")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val member = event.member ?: return
        if (member.user.isBot) return

        try {
            play(event.channel, member, event.message.contentRaw)
        } catch (e: Exception) {
            event.channel.sendMessage(e.message ?: e.toString()).queue()
        }
    }
}

fun play(channel: TextChannel, member: Member, message: String) {
    if (message.startsWith(";;play ")) {
        val vc = member.voiceState?.channel
        if (vc == null) {
            channel.sendMessage("You must be in a voice channel").queue()
            return
        }

        val link = lavalink.getLink(channel.guild)
        link.connect(vc)

        val track = message.drop(";;play ".length).trim()
        playerManager.loadItem(track, object : AudioLoadResultHandler {
            override fun loadFailed(e: FriendlyException) {
                channel.sendMessage(e.message ?: e.toString()).queue()
            }

            override fun trackLoaded(track: AudioTrack) {
                channel.sendMessage("Playing " + track.info.title).queue()
                link.player.playTrack(track)
            }

            override fun noMatches() {
                channel.sendMessage("No matches").queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val track = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                if (track == null) {
                    channel.sendMessage("Empty playlist").queue()
                    return
                }
                channel.sendMessage("Playing " + track.info.title).queue()
                link.player.playTrack(track)
            }
        })
    }
}