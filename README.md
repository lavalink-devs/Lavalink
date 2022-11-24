# Lavalink
A standalone audio sending node based on [Lavaplayer](https://github.com/sedmelluq/lavaplayer) and [Koe](https://github.com/KyokoBot/koe).
Allows for sending audio without it ever reaching any of your shards.

Being used in production by FredBoat, Dyno, LewdBot, and more.

A [basic example bot](Testbot) is available.

[![JDA guild](https://discordapp.com/api/guilds/125227483518861312/embed.png?style=banner2)](https://discord.gg/jtAWrzU)

## Features
* Powered by Lavaplayer
* Minimal CPU/memory footprint
* Twitch/YouTube stream support
* Event system
* Seeking
* Volume control
* REST API for resolving Lavaplayer tracks
* Statistics (good for load balancing)
* Basic authentication
* Prometheus metrics
* Docker images
* [Plugin support](PLUGINS.md) (beta)

## Requirements

* Java 11* LTS or newer required.
* OpenJDK or Zulu running on Linux AMD64 is officially supported.

Support for Darwin (Mac), Windows AMD64, and Linux ARM (Raspberry Pi) is provided on a best-effort basis. This is dependent on Lavaplayer's native libraries.

Lavaplayer currently supports following architectures: 

`Darwin (M1 & Intel)`, `Linux aarch32`, `Linux aarch64`, `Linux ARMv7+ 32/64`, `Linux ARMHF(v6) 32`, `Linux i386 32`, `Linux x86 64`, `Windows i386 32` and `Windows x86 64`

JDA-NAS(Native Audio Buffer) & the Timescale filter are currently not supported on following architectures: 

`Linux ARMHF(v6) 32` and `Linux aarch32`


Support for other JVMs is also best-effort. Periodic CPU utilization stats are prone not to work everywhere.

**\*Java 11 appears to have some issues with Discord's TLS 1.3. Java 14 has other undiagnosed HTTPS problems. Use Java 13. Docker images have been updated.** See [#258](https://github.com/freyacodes/Lavalink/issues/258), [#260](https://github.com/freyacodes/Lavalink/issues/260)

## Changelog

Please see [here](CHANGELOG.md)

## Versioning policy

- The public API ("API" in a very broad sense) of Lavalink can be categorized into two main domains:
  - **Client Domain:** The API exposed to clients, consisting of both the WebSocket protocol and any public HTTP endpoints
  - **Server Domain:** The server application with its runtime environment, its configuration, etc.

- A change that is breaking to one domain might not be breaking at all to another.

  *Examples:*
  - Removing an endpoint: This is a breaking change for the client domain but is not for running the server itself.
  - Upgrading the minimum Java version: This is a breaking change for the server domain, but client implementations couldn't care less about it.

**Given the above, the following versioning pattern lends itself well to the Lavalink project:**

_**api.major.minor.patch**_

- **API**: Bumped when breaking changes are committed to the client domain of Lavalink

  *Examples:* Removing an endpoint, altering the output of an endpoint in a non-backward-compatible manner
- **Major**: Bumped when breaking changes are committed to the Lavalink server domain

  *Examples:* Bumping the required Java version, altering the configuration in a non-backward-compatible manner
- **Minor**: New features in any domain

  *Examples:* New optional endpoint or opcode, additional configuration options, change of large subsystems or dependencies
- **Patch**: Bug fixes in any domain

Examples: Fixing a race condition, fixing unexpected exceptions, fixing output that is not according to specs, etc.

While major, minor and patch will do an optimum effort to adhere to [Semantic Versioning](https://semver.org/), prepending it with an additional API version makes life easier for developers in two ways: It is a clear way for the Lavalink project to communicate the relevant breaking changes to client developers, and in return, client developers can use the API version to communicate to their users about the compatibility of their clients to the Lavalink server.


## Client libraries:
Client | Platform | Compatible With | Additional Information
-------|----------|-----------------|-----------------------
[Lavalink-Client](https://github.com/freyacodes/lavalink-client) | Java | JDA/**Any**
[LavaClient](https://github.com/HoneycombsTeam/LavaClient) | Java | JDA
[Lavalink.kt](https://github.com/DRSchlaubi/lavalink.kt) | Kotlin | JDA/Kord/**Any** | Kotlin Coroutines
[Lavalink.py](https://github.com/Devoxin/Lavalink.py) | Python | **Any**
[lavasnek_rs](https://github.com/vicky5124/lavasnek_rs) | Python | **Any\*** | *`asyncio`-based libraries only
[lavaplayer-py](https://github.com/HazemMeqdad/lavaplayer) | Python | **Any\*** | *`asyncio`-based libraries only
[Wavelink](https://github.com/PythonistaGuild/Wavelink) | Python | discord.py **V2**
[Pomice](https://github.com/cloudwithax/pomice) | Python | discord.py **V2**
[discord-ext-lava](https://github.com/Axelware/discord-ext-lava) | Python | discord.py
[Lavapy](https://github.com/Aspect1103/Lavapy) | Python | discord.py
[Magma](https://github.com/initzx/magma) | Python | discord.py
[interactions-lavalink](https://github.com/interactions-py/lavalink) | Python | interactions.py
[Lavadeno](https://github.com/lavaclient/lavadeno) | Deno | **Any**
[LavaJS](https://github.com/OverleapTechnologies/LavaJS) | Node.js | **Any**
[LavaClient](https://github.com/lavaclient/lavaclient) | Node.js | **Any**
[Lavacoffee](https://github.com/XzFirzal/lavacoffee) | Node.js | **Any**
[Lavacord](https://github.com/lavacord/lavacord) | Node.js | **Any**
[FastLink](https://github.com/ThePedroo/FastLink) | Node.js | **Any**
[Moonlink.js](https://github.com/1Lucas1apk/moonlink.js) | Node.js | **Any**
[@skyra/audio](https://github.com/skyra-project/audio) | Node.js | discord.js | Archived
[Poru](https://github.com/parasop/poru) | Node.js | **Any**
[Shoukaku](https://github.com/Deivu/Shoukaku) | Node.js | **Any**
[Lavaudio](https://github.com/rilysh/lavaudio) | Node.js | **Any**
[Gorilink](https://github.com/Gorillas-Team/Gorilink) | Node.js | discord.js | Archived/Unmaintained
[SandySounds](https://github.com/MrJohnCoder/SandySounds) | Node.js | **Any** | Unmaintained
[eris-lavalink](https://github.com/briantanner/eris-lavalink) | Node.js | eris | Unmaintained
[Victoria](https://github.com/Yucked/Victoria) | .NET | Discord.Net
[Lavalink4NET](https://github.com/angelobreuer/Lavalink4NET) | .NET | Discord\.Net/DSharpPlus
[DSharpPlus.Lavalink](https://github.com/DSharpPlus/DSharpPlus/tree/master/DSharpPlus.Lavalink) | .NET | DSharpPlus
[Lavalink.NET](https://github.com/Dev-Yukine/Lavalink.NET) | .NET | **Any** | Unmaintained
[SharpLink](https://github.com/Devoxin/SharpLink) | .NET | Discord.Net | Unmaintained
[DisCatSharp.Lavalink](https://github.com/Aiko-IT-Systems/DisCatSharp/tree/main/DisCatSharp.Lavalink) | .NET | DisCatSharp
[disgolink](https://github.com/DisgoOrg/disgolink) | Go | Disgo/DiscordGo/**Any**
[waterlink](https://github.com/lukasl-dev/waterlink) | Go | **Any**
[gavalink](https://github.com/foxbot/gavalink) | Go | **Any** | Unmaintained
[Lavalink-rs](https://gitlab.com/vicky5124/lavalink-rs) | Rust | **Any\*** | *`tokio`-based libraries only
[Coglink](https://github.com/ThePedroo/Coglink) | C | Concord

Or alternatively, you can create your own client library, following the [implementation documentation](https://github.com/freyacodes/Lavalink/blob/master/IMPLEMENTATION.md).
Any client libraries marked with `Unmaintained` have been marked as such as their repositories have not received any commits for at least 1 year since time of checking,
however they are listed as they may still support Lavalink, and/or have not needed maintenance, however keep in mind that compatibility and full feature support is not guaranteed.

## Server configuration
Download binaries from [the GitHub actions](https://github.com/freyacodes/Lavalink/actions) or [the GitHub releases](https://github.com/freyacodes/Lavalink/releases)(specific versions prior to `v3.5` can be found in the [CI Server](https://ci.fredboat.com/viewLog.html?buildId=lastSuccessful&buildTypeId=Lavalink_Build&tab=artifacts&guest=1)). 

Put an `application.yml` file in your working directory. ([Example here](https://github.com/freyacodes/Lavalink/blob/master/LavalinkServer/application.yml.example))

Run with `java -jar Lavalink.jar`

Docker images are available on the [Docker Hub](https://hub.docker.com/r/fredboat/lavalink/).

[![Docker Pulls](https://img.shields.io/docker/pulls/fredboat/lavalink.svg)](https://hub.docker.com/r/fredboat/lavalink/)
