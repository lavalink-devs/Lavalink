package lavalink.testbot

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
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
import java.lang.IllegalArgumentException
import java.net.URI

private val log: Logger = LoggerFactory.getLogger("Testbot")
private lateinit var jda: JDA
private val lavalink = JdaLavalink(1) { _ -> jda }
lateinit var host: String
lateinit var password: String

fun main(args: Array<String>) {
    if (args.size < 3) {
        throw IllegalArgumentException("Expected 3 arguments. Please refer to the readme.")
    }

    val token = args[0]
    host = args[1]
    password = args[2]

    jda = JDABuilder.createDefault(token,
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
        lavalink.addNode(URI(host), password)
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
        link.node?.restClient?.loadItem(track, object : AudioLoadResultHandler {
            override fun loadFailed(e: FriendlyException) {
                channel.sendMessage(e.message ?: e.toString()).queue()
            }

            override fun trackLoaded(track: AudioTrack) {
                channel.sendMessage("Playing `${track.info.title}`").queue()
                link.player.playTrack(track)
            }

            override fun noMatches() {
                channel.sendMessage("No matches").queue()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val loaded = playlist.selectedTrack ?: playlist.tracks.firstOrNull()
                if (loaded == null) {
                    channel.sendMessage("Empty playlist").queue()
                    return
                }
                channel.sendMessage("Playing `${loaded.info.title}` from list `${playlist.name}`").queue()
                link.player.playTrack(loaded)
            }
        })
    }
}