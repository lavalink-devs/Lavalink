# Lavalink

<img align="right" src="/branding/lavalink.svg" width=200 alt="Lavalink logo">

A standalone audio sending node based on [Lavaplayer](https://github.com/sedmelluq/lavaplayer) and [Koe](https://github.com/KyokoBot/koe).
Allows for sending audio without it ever reaching any of your shards.

Being used in production by FredBoat, Dyno, LewdBot, and more.

A [basic example bot](Testbot) is available.

[![Lavalink Guild](https://discordapp.com/api/guilds/1082302532421943407/embed.png?style=banner2)](https://discord.gg/ZW4s47Ppw4)

> **Warning** Lavalink v4 is now in beta!
> See [here](CHANGELOG.md#400-beta1) for more information.

<details>
<summary>Table of Contents</summary>

- [Features](#features)
- [Requirements](#requirements)
- [Changelog](#changelog)
- [Versioning policy](#versioning-policy)
- [Client libraries](#client-libraries)
- [Server configuration](#server-configuration)
  - [Config](#config)
  - [Binary](#binary)
  - [Docker](#docker)

</details>

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

* Java 17 LTS or newer required. (we recommend running the latest LTS version or newer)
* OpenJDK or Zulu running on Linux AMD64 is officially supported.

Support for Darwin (Mac), Windows AMD64, and Linux ARM (Raspberry Pi) is provided on a best-effort basis. This is dependent on Lavaplayer's native libraries.

Lavaplayer currently supports following architectures: 

`Darwin (M1 & Intel)`, `Linux aarch32`, `Linux aarch64`, `Linux ARMv7+ 32/64`, `Linux ARMHF(v6) 32`, `Linux i386 32`, `Linux x86 64`, `Windows i386 32` and `Windows x86 64`

JDA-NAS(Native Audio Buffer) & the Timescale filter are currently not supported on following architectures: 

`Linux ARMHF(v6) 32` and `Linux aarch32`


Support for other JVMs is also best-effort. Periodic CPU utilization stats are prone not to work everywhere.

## Changelog

Please see [here](CHANGELOG.md)

## Versioning policy

Lavalink follows [Semantic Versioning](https://semver.org/).

The version number is composed of the following parts:

    MAJOR breaking API changes
    MINOR new backwards compatible features
    PATCH backwards compatible bug fixes
    PRERELEASE pre-release version
    BUILD additional build metadata

Version numbers can come in different combinations, depending on the release type:

    `MAJOR.MINOR.PATCH` - Stable release
    `MAJOR.MINOR.PATCH+BUILD` - Stable release with additional build metadata
    `MAJOR.MINOR.PATCH-PRERELEASE` - Pre-release
    `MAJOR.MINOR.PATCH-PRERELEASE+BUILD` - Pre-release additional build metadata

---

## Client libraries:
| Client                                                      | Platform | Compatible With                            | Additional Information |
|-------------------------------------------------------------|----------|--------------------------------------------|------------------------|
| [DisGoLink](https://github.com/disgoorg/disgolink)          | Go       | **Any**                                    |                        |
| [Mafic](https://github.com/ooliver1/mafic)                  | Python   | discord.py **V2**/nextcord/disnake/py-cord |                        |
| [Moonlink.js](https://github.com/1Lucas1apk/moonlink.js)    | Node.js  | **Any**                                    |                        |
| [Magmastream](https://github.com/Blackfort-Hosting/magmastream)    | Node.js      | **Any**                         |                        |

<details>
<summary>v3.7 supporting Client Libraries</summary>

| Client                                                      | Platform | Compatible With                            | Additional Information          |
|-------------------------------------------------------------|----------|--------------------------------------------|---------------------------------|
| [Lavalink.kt](https://github.com/DRSchlaubi/lavalink.kt)    | Kotlin   | JDA/Kord/**Any**                           | Kotlin Coroutines               |
| [lavaplay.py](https://github.com/HazemMeqdad/lavaplay.py)   | Python   | **Any\***                                  | *`asyncio`-based libraries only |
| [Mafic](https://github.com/ooliver1/mafic)                  | Python   | discord.py **V2**/nextcord/disnake/py-cord |                                 |
| [Wavelink](https://github.com/PythonistaGuild/Wavelink)     | Python   | discord.py **V2**                          |                                 |
| [Pomice](https://github.com/cloudwithax/pomice)             | Python   | discord.py **V2**                          |                                 |
| [Lavacord](https://github.com/lavacord/lavacord)            | Node.js  | **Any**                                    |                                 |
| [Poru](https://github.com/parasop/poru)                     | Node.js  | **Any**                                    |                                 |
| [Shoukaku](https://github.com/Deivu/Shoukaku)               | Node.js  | **Any**                                    |                                 |
| [Cosmicord.js](https://github.com/SudhanPlayz/Cosmicord.js) | Node.js  | **Any**                                    |                                 |
| [Nomia](https://github.com/DHCPCD9/Nomia)                   | .NET     | DSharpPlus                                 |                                 |
| [DisGoLink](https://github.com/disgoorg/disgolink)          | Go       | **Any**                                    |                                 |
| [Coglink](https://github.com/ThePedroo/Coglink)             | C        | Concord                                    |                                 |

</details>

Or alternatively, you can create your own client library, following the [implementation documentation](IMPLEMENTATION.md).
Any client libraries marked with `Unmaintained` have been marked as such as their repositories have not received any commits for at least 1 year since time of checking,
however they are listed as they may still support Lavalink, and/or have not needed maintenance, however keep in mind that compatibility and full feature support is not guaranteed.

## Server configuration

### Config

The server configuration is done in `application.yml`. You can find an example configuration [here](LavalinkServer/application.yml.example).

Alternatively, you can also use environment variables to configure the server. The environment variables are named the same as the keys in the `application.yml` file, but in uppercase and with `.` replaced with `_`. For example, `server.port` becomes `SERVER_PORT`.
For arrays, the index is appended to the key, starting at 0. For example, `LAVALINK_PLUGINS_0_DEPENDENCY` refers to the `dependency` key of the first plugin.

<details>
<summary>List of all env keys</summary>

```env
SERVER_PORT
SERVER_ADDRESS

LAVALINK_PLUGINS_0_DEPENDENCY
LAVALINK_PLUGINS_0_REPOSITORY

LAVALINK_PLUGINS_1_DEPENDENCY
LAVALINK_PLUGINS_1_REPOSITORY

LAVALINK_SERVER_PASSWORD
LAVALINK_SERVER_SOURCES_YOUTUBE
LAVALINK_SERVER_SOURCES_BANDCAMP
LAVALINK_SERVER_SOURCES_SOUNDCLOUD
LAVALINK_SERVER_SOURCES_TWITCH
LAVALINK_SERVER_SOURCES_VIMEO
LAVALINK_SERVER_SOURCES_HTTP
LAVALINK_SERVER_SOURCES_LOCAL

LAVALINK_SERVER_FILTERS_VOLUME
LAVALINK_SERVER_FILTERS_EQUALIZER
LAVALINK_SERVER_FILTERS_KARAOKE
LAVALINK_SERVER_FILTERS_TIMESCALE
LAVALINK_SERVER_FILTERS_TREMOLO
LAVALINK_SERVER_FILTERS_VIBRATO
LAVALINK_SERVER_FILTERS_DISTORTION
LAVALINK_SERVER_FILTERS_ROTATION
LAVALINK_SERVER_FILTERS_CHANNEL_MIX
LAVALINK_SERVER_FILTERS_LOW_PASS

LAVALINK_SERVER_BUFFER_DURATION_MS
LAVALINK_SERVER_FRAME_BUFFER_DURATION_MS
LAVALINK_SERVER_OPUS_ENCODING_QUALITY
LAVALINK_SERVER_RESAMPLING_QUALITY
LAVALINK_SERVER_TRACK_STUCK_THRESHOLD_MS
LAVALINK_SERVER_USE_SEEK_GHOSTING

LAVALINK_SERVER_PLAYER_UPDATE_INTERVAL
LAVALINK_SERVER_YOUTUBE_SEARCH_ENABLED
LAVALINK_SERVER_SOUNDCLOUD_SEARCH_ENABLED

LAVALINK_SERVER_GC_WARNINGS

LAVALINK_SERVER_RATELIMIT_IP_BLOCKS
LAVALINK_SERVER_RATELIMIT_EXCLUDE_IPS
LAVALINK_SERVER_RATELIMIT_STRATEGY
LAVALINK_SERVER_RATELIMIT_SEARCH_TRIGGERS_FAIK
LAVALINK_SERVER_RATELIMIT_RETRY_LIMIT

LAVALINK_SERVER_YOUTUBE_CONFIG_EMAIL
LAVALINK_SERVER_YOUTUBE_CONFIG_PASSWORD

LAVALINK_SERVER_HTTP_CONFIG_PROXY_HOST
LAVALINK_SERVER_HTTP_CONFIG_PROXY_PORT
LAVALINK_SERVER_HTTP_CONFIG_PROXY_USER
LAVALINK_SERVER_HTTP_CONFIG_PROXY_PASSWORD

METRICS_PROMETHEUS_ENABLED
METRICS_PROMETHEUS_ENDPOINT

SENTRY_DSN
SENTRY_ENVIRONMENT
SENTRY_TAGS_SOME_KEY
SENTRY_TAGS_ANOTHER_KEY

LOGGING_FILE_PATH
LOGGING_LEVEL_ROOT
LOGGING_LEVEL_LAVALINK

LOGGING_REQUEST_ENABLED
LOGGING_REQUEST_INCLUDE_CLIENT_INFO
LOGGING_REQUEST_INCLUDE_HEADERS
LOGGING_REQUEST_INCLUDE_QUERY_STRING
LOGGING_REQUEST_INCLUDE_PAYLOAD
LOGGING_REQUEST_MAX_PAYLOAD_LENGTH

LOGGING_LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE
LOGGING_LOGBACK_ROLLINGPOLICY_MAX_HISTORY
```
</details>


### Binary
Download binaries from the [Download Server](https://repo.arbjerg.dev/artifacts/lavalink/), [GitHub releases](https://github.com/lavalink-devs/Lavalink/releases) (specific versions prior to `v3.5` can be found in the [CI Server](https://ci.fredboat.com/viewLog.html?buildId=lastSuccessful&buildTypeId=Lavalink_Build&tab=artifacts&guest=1)) or [GitHub actions](https://github.com/lavalink-devs/Lavalink/actions).

Put an `application.yml` file in your working directory. ([Example here](LavalinkServer/application.yml.example))

Run with `java -jar Lavalink.jar` from the same directory

### Docker

Docker images can be found under [packages](https://github.com/lavalink-devs/Lavalink/pkgs/container/lavalink) with old builds prior to `v3.7.4` being available on [Docker Hub](https://hub.docker.com/r/fredboat/lavalink/).
There are 2 image variants `Ubuntu` and `Alpine`, the `Alpine` variant is smaller and can be used with the `-alpine` suffix, for example `ghcr.io/freyacodes/lavalink:3-alpine`.

---

Install [Docker](https://docs.docker.com/engine/install/) & [Docker Compose](https://docs.docker.com/compose/install/)

Create a `docker-compose.yml` with the following content:
```yaml
version: "3.8"

services:
    lavalink:
        image: ghcr.io/lavalink-devs/lavalink:4 # pin the image version to Lavalink v4
        container_name: lavalink
        restart: unless-stopped
        environment:
            - _JAVA_OPTIONS=-Xmx6G # set Java options here
            - SERVER_PORT=2333 # set lavalink server port
            - LAVALINK_SERVER_PASSWORD=youshallnotpass # set password for lavalink
        volumes:
            - ./application.yml:/opt/Lavalink/application.yml # mount application.yml from the same directory or use environment variables
            - ./plugins/:/opt/Lavalink/plugins/ # persist plugins between restarts, make sure to set the correct permissions (user: 322, group: 322)
        networks:
            - lavalink
        expose:
            - 2333 # lavalink exposes port 2333 to connect to for other containers (this is for documentation purposes only)
        ports:
            - 2333:2333 # you only need this if you want to make your lavalink accessible from outside of containers
networks:
    lavalink: # create a lavalink network you can add other containers to, to give them access to Lavalink
        name: lavalink
```

Run `docker compose up -d`. See [Docker Compose Up](https://docs.docker.com/engine/reference/commandline/compose_up/)

If your bot also runs in a docker container you can make that container join the lavalink network and use `lavalink` (service name) as the hostname to connect.
See [Docker Networking](https://docs.docker.com/network/) & [Docker Compose Networking](https://docs.docker.com/compose/networking/)

