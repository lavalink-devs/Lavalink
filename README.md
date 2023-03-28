# Lavalink
A standalone audio sending node based on [Lavaplayer](https://github.com/sedmelluq/lavaplayer) and [Koe](https://github.com/KyokoBot/koe).
Allows for sending audio without it ever reaching any of your shards.

Being used in production by FredBoat, Dyno, LewdBot, and more.

A [basic example bot](Testbot) is available.

[![Lavalink Guild](https://discordapp.com/api/guilds/1082302532421943407/embed.png?style=banner2)](https://discord.gg/ZW4s47Ppw4)

## Features
* Powered by Lavaplayer
* Minimal CPU/memory footprint
* Twitch/YouTube stream support
* Event system
* Seeking
* Volume control
* REST API for resolving Lavaplayer tracks, controlling players, and more
* Statistics (good for load balancing)
* Basic authentication
* Prometheus metrics
* Docker images
* [Plugin support](PLUGINS.md)

## Requirements

* Java 11 LTS or newer required. (we recommend running the latest LTS version or newer)
* OpenJDK or Zulu running on Linux AMD64 is officially supported.

Support for Darwin (Mac), Windows AMD64, and Linux ARM (Raspberry Pi) is provided on a best-effort basis. This is dependent on Lavaplayer's native libraries.

Lavaplayer currently supports following architectures: 

`Darwin (M1 & Intel)`, `Linux aarch32`, `Linux aarch64`, `Linux ARMv7+ 32/64`, `Linux ARMHF(v6) 32`, `Linux i386 32`, `Linux x86 64`, `Windows i386 32` and `Windows x86 64`

JDA-NAS(Native Audio Buffer) & the Timescale filter are currently not supported on following architectures: 

`Linux ARMHF(v6) 32` and `Linux aarch32`

## Changelog

Please see [here](CHANGELOG.md)

## Versioning policy

Lavalink follows [Semantic Versioning](https://semver.org/).

Given a version number `MAJOR.MINOR.PATCH`, the following rules apply:

    MAJOR breaking API changes
    MINOR new backwards compatible features
    PATCH backwards compatible bug fixes

Additional labels for release candidates are available as extensions to the `MAJOR.MINOR.PATCH-rcNUMBER`(`3.6.0-rc.1`) format.


## Client libraries:
| Client                                                                                                | Platform | Compatible With                            | Additional Information          |
|-------------------------------------------------------------------------------------------------------|----------|--------------------------------------------|---------------------------------|


<details>
<summary>v3.7 supporting Client Libraries</summary>

| Client                                                  | Platform | Compatible With                            | Additional Information |
|---------------------------------------------------------|----------|--------------------------------------------|------------------------|
| [Mafic](https://github.com/ooliver1/mafic)              | Python   | discord.py **V2**/nextcord/disnake/py-cord |                        |
| [Wavelink](https://github.com/PythonistaGuild/Wavelink) | Python   | discord.py **V2**                          |                        |
| [Pomice](https://github.com/cloudwithax/pomice)         | Python   | discord.py **V2**                          |                        |
| [Lavacord](https://github.com/lavacord/lavacord)        | Node.js  | **Any**                                    |                        |
| [Poru](https://github.com/parasop/poru)                 | Node.js  | **Any**                                    |                        |
| [Shoukaku](https://github.com/Deivu/Shoukaku)           | Node.js  | **Any**                                    |                        |
| [Nomia](https://github.com/DHCPCD9/Nomia)               | .NET     | DSharpPlus                                 |                        |
| [DisGoLink](https://github.com/disgoorg/disgolink)      | Go       | **Any**                                    |                        |
| [Coglink](https://github.com/ThePedroo/Coglink)         | C        | Concord                                    |                        |

</details>

Or alternatively, you can create your own client library, following the [implementation documentation](IMPLEMENTATION.md).
Any client libraries marked with `Unmaintained` have been marked as such as their repositories have not received any commits for at least 1 year since time of checking,
however they are listed as they may still support Lavalink, and/or have not needed maintenance, however keep in mind that compatibility and full feature support is not guaranteed.

## Server configuration
Download binaries from [the GitHub actions](https://github.com/freyacodes/Lavalink/actions) or [the GitHub releases](https://github.com/freyacodes/Lavalink/releases)(specific versions prior to `v3.5` can be found in the [CI Server](https://ci.fredboat.com/viewLog.html?buildId=lastSuccessful&buildTypeId=Lavalink_Build&tab=artifacts&guest=1)). 

Put an `application.yml` file in your working directory. ([Example here](https://github.com/freyacodes/Lavalink/blob/master/LavalinkServer/application.yml.example))

Run with `java -jar Lavalink.jar`

Docker images can be found under [packages](https://github.com/freyacodes/Lavalink/pkgs/container/lavalink) with old builds prior to `v3.7.4` being available on [Docker Hub](https://hub.docker.com/r/fredboat/lavalink/).
