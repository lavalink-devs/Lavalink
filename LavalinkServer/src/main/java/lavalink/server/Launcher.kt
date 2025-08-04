/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import lavalink.server.bootstrap.PluginManager
import lavalink.server.info.AppInfo
import lavalink.server.info.GitRepoState
import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.core.io.DefaultResourceLoader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess


@Suppress("SpringComponentScan")
@SpringBootApplication
@ComponentScan(
    value = ["\${componentScan}"],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [PluginManager::class])]
)
class LavalinkApplication

object Launcher {

    private val log = LoggerFactory.getLogger(Launcher::class.java)

    val startTime = System.currentTimeMillis()

    private fun getVersionInfo(indentation: String = "\t", vanity: Boolean = true): String {
        val appInfo = try {
            AppInfo()
        } catch (e: Exception) {
            log.warn("Failed to load application info", e)
            null
        }
        
        val gitRepoState = try {
            GitRepoState()
        } catch (e: Exception) {
            log.warn("Failed to load Git repository state", e)
            null
        }

        val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")
            .withZone(ZoneId.of("UTC"))
        
        val buildTime = appInfo?.buildTime?.let { dtf.format(Instant.ofEpochMilli(it)) } ?: "Unknown"
        val commitTime = gitRepoState?.commitTime?.let { dtf.format(Instant.ofEpochMilli(it * 1000)) } ?: "Unknown"
        val version = appInfo?.versionBuild?.takeUnless { it.startsWith("@") } ?: "Unknown"

        return buildString {
            if (vanity) {
                appendLine()
                appendLine()
                appendLine(getVanity())
            }
            if (gitRepoState?.isLoaded != true) {
                appendLine()
                appendLine("$indentation*** Unable to find or load Git metadata ***")
            }
            appendLine()
            append("${indentation}Version:        "); appendLine(version)
            if (gitRepoState?.isLoaded == true) {
                append("${indentation}Build time:     "); appendLine(buildTime)
                append("${indentation}Branch          "); appendLine(gitRepoState.branch)
                append("${indentation}Commit:         "); appendLine(gitRepoState.commitIdAbbrev)
                append("${indentation}Commit time:    "); appendLine(commitTime)
            }
            append("${indentation}JVM:            "); appendLine(System.getProperty("java.version"))
            append("${indentation}Lavaplayer      "); appendLine(PlayerLibrary.VERSION)
        }
    }

    private fun getVanity(): String {
        val red = "\u001B[31m"
        val green = "\u001B[32m"
        val defaultC = "\u001B[0m"

        val vanity = StringBuilder()
            .append(green).append("       .  ").append(red).append(" _                  _ _       _    ").append(green).append("__ _ _\n")
            .append(green).append("      /\\\\ ").append(red).append("| | __ ___   ____ _| (_)_ __ | | __").append(green).append("\\ \\ \\ \\\n")
            .append(green).append("     ( ( )").append(red).append("| |/ _` \\ \\ / / _` | | | '_ \\| |/ /").append(green).append(" \\ \\ \\ \\\n")
            .append(green).append("      \\\\/ ").append(red).append("| | (_| |\\ V / (_| | | | | | |   < ").append(green).append("  ) ) ) )\n")
            .append(green).append("       '  ").append(red).append("|_|\\__,_| \\_/ \\__,_|_|_|_| |_|_|\\_\\").append(green).append(" / / / /\n")
            .append(defaultC).append("    =========================================").append(green).append("/_/_/_/").append(defaultC)

        return vanity.toString()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            if (args.isNotEmpty() &&
                (args[0].equals("-v", ignoreCase = true) || args[0].equals("--version", ignoreCase = true))
            ) {
                println(getVersionInfo(indentation = "", vanity = false))
                return
            }

            val parent = launchPluginBootstrap(args)
            launchMain(parent, args)
        } catch (e: Exception) {
            log.error("Failed to start Lavalink application", e)
            exitProcess(1)
        }
    }

    private fun launchPluginBootstrap(args: Array<String>): ConfigurableApplicationContext {
        return try {
            SpringApplication(PluginManager::class.java).run {
                setBannerMode(Banner.Mode.OFF)
                webApplicationType = WebApplicationType.NONE
                run(*args)
            }
        } catch (e: Exception) {
            log.error("Failed to launch plugin bootstrap", e)
            throw RuntimeException("Plugin bootstrap initialization failed", e)
        }
    }

    private fun launchMain(parent: ConfigurableApplicationContext, args: Array<String>) {
        try {
            val pluginManager = parent.getBean(PluginManager::class.java)
            val properties = Properties()
            properties["componentScan"] = pluginManager.pluginManifests.map { it.path }
                .toMutableList().apply { add("lavalink.server") }

            SpringApplicationBuilder()
                .sources(LavalinkApplication::class.java)
                .properties(properties)
                .web(WebApplicationType.SERVLET)
                .bannerMode(Banner.Mode.OFF)
                .resourceLoader(DefaultResourceLoader(pluginManager.classLoader))
                .listeners(
                    ApplicationListener { event: Any ->
                        when (event) {
                            is ApplicationEnvironmentPreparedEvent -> {
                                log.info(getVersionInfo())
                            }

                            is ApplicationReadyEvent -> {
                                val startupTime = System.currentTimeMillis() - startTime
                                log.info("Lavalink is ready to accept connections. Startup completed in ${startupTime}ms")
                            }

                            is ApplicationFailedEvent -> {
                                log.error("Application failed", event.exception)
                            }
                        }
                    }
                ).parent(parent)
                .run(*args)
        } catch (e: Exception) {
            log.error("Failed to launch main application", e)
            parent.close()
            throw RuntimeException("Main application initialization failed", e)
        }
    }
}
