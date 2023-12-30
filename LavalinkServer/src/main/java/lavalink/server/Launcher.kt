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
        val appInfo = AppInfo()
        val gitRepoState = GitRepoState()

        val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")
            .withZone(ZoneId.of("UTC"))
        val buildTime = dtf.format(Instant.ofEpochMilli(appInfo.buildTime))
        val commitTime = dtf.format(Instant.ofEpochMilli(gitRepoState.commitTime * 1000))
        val version = appInfo.versionBuild.takeUnless { it.startsWith("@") }
            ?: "Unknown"

        return buildString {
            if (vanity) {
                appendLine()
                appendLine()
                appendLine(getVanity())
            }
            if (!gitRepoState.isLoaded) {
                appendLine()
                appendLine("$indentation*** Unable to find or load Git metadata ***")
            }
            appendLine()
            append("${indentation}Version:        "); appendLine(version)
            if (gitRepoState.isLoaded) {
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
        //ansi color codes
        val red = "[31m"
        val green = "[32m"
        val defaultC = "[0m"

        var vanity = ("g       .  r _                  _ _       _    g__ _ _\n"
                + "g      /\\\\ r| | __ ___   ____ _| (_)_ __ | | __g\\ \\ \\ \\\n"
                + "g     ( ( )r| |/ _` \\ \\ / / _` | | | '_ \\| |/ /g \\ \\ \\ \\\n"
                + "g      \\\\/ r| | (_| |\\ V / (_| | | | | | |   < g  ) ) ) )\n"
                + "g       '  r|_|\\__,_| \\_/ \\__,_|_|_|_| |_|_|\\_\\g / / / /\n"
                + "d    =========================================g/_/_/_/d")

        vanity = vanity.replace("r".toRegex(), red)
        vanity = vanity.replace("g".toRegex(), green)
        vanity = vanity.replace("d".toRegex(), defaultC)
        return vanity
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isNotEmpty() &&
            (args[0].equals("-v", ignoreCase = true) || args[0].equals("--version", ignoreCase = true))
        ) {
            println(getVersionInfo(indentation = "", vanity = false))
            return
        }

        val parent = launchPluginBootstrap()
        launchMain(parent, args)
    }

    private fun launchPluginBootstrap() = SpringApplication(PluginManager::class.java).run {
        setBannerMode(Banner.Mode.OFF)
        webApplicationType = WebApplicationType.NONE
        run()
    }

    private fun launchMain(parent: ConfigurableApplicationContext, args: Array<String>) {
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
                            log.info("Lavalink is ready to accept connections.")
                        }

                        is ApplicationFailedEvent -> {
                            log.error("Application failed", event.exception)
                        }
                    }
                }
            ).parent(parent)
            .run(*args)
    }
}
