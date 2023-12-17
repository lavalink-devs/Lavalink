# Lavalink

<img align="right" src="/branding/lavalink.svg" width=200 alt="Lavalink logo">

A standalone audio sending node based on [Lavaplayer](https://github.com/lavalink-devs/lavaplayer) and [Koe](https://github.com/KyokoBot/koe).
Allows for sending audio without it ever reaching any of your shards.

Being used in production by FredBoat, Dyno, LewdBot, and more.

A [basic example bot](Testbot) is available.

[![Lavalink Guild](https://discordapp.com/api/guilds/1082302532421943407/embed.png?style=banner2)](https://discord.gg/ZW4s47Ppw4)

> [!NOTE]
> Lavalink v4 is now **out** of beta! See [the changelog](CHANGELOG.md) for more information.

## Getting started
* Pick one of the [up-to-date clients](https://lavalink.dev/clients). Advanced users can create their own using the [API documentation
](https://lavalink.dev/api/)
* See the [server configuration documentation](https://lavalink.dev/configuration/) for configuring your Lavalink server
* Explore [available plugins](https://lavalink.dev/plugins) for extra features
* See also our [FAQ](https://lavalink.dev/getting-started/faq)
<details>
<summary>Table of Contents</summary>

- [Features](#features)
- [Requirements](#requirements)
- [Hardware Support](#hardware-support)
- [Changelog](#changelog)
- [Versioning policy](#versioning-policy)

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
* [Plugin support](https://lavalink.dev/plugins.html)

## Requirements

* Java 17 LTS or newer required. (we recommend running the latest LTS version or newer)
* OpenJDK or Zulu running on Linux AMD64 is officially supported.

Support for other JVMs is also best-effort. Periodic CPU utilization stats are prone not to work everywhere.

## Hardware Support

Lavalink also runs on other hardware, but support is best-effort.
Here is a list of known working hardware:

| Operating System | Architecture | Lavaplayer | JDA-NAS | Timescale | AVX2 |
|------------------|--------------|------------|---------|-----------|------|
| linux            | x86-64       | ✅          | ✅       | ✅         | ✅    |
| linux            | x86          | ✅          | ✅       | ✅         | ✅    |
| linux            | arm          | ✅          | ✅       | ✅         | ❌    |
| linux            | armhf        | ✅          | ❌       | ❌         | ❌    |
| linux            | aarch32      | ✅          | ❌       | ❌         | ❌    |
| linux            | aarch64      | ✅          | ✅       | ✅         | ❌    |
| linux-musl       | x86-64       | ✅          | ✅       | ✅         | ✅    |
| linux-musl       | aarch64      | ✅          | ✅       | ✅         | ❌    |
| windows          | x86-64       | ✅          | ✅       | ✅         | ✅    |
| Windows          | x86          | ✅          | ✅       | ✅         | ✅    |
| darwin           | x86-64       | ✅          | ✅       | ✅         | ✅    |
| darwin           | aarch64e     | ✅          | ✅       | ✅         | ❌    |

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



