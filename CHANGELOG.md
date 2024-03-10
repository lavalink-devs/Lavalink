# Changelog

Each release usually includes various fixes and improvements.
The most noteworthy of these, as well as any features and breaking changes, are listed here.

## v4.0.4
* Update Lavaplayer to `2.1.1`

## v4.0.3
* Fixed plugins not registering (introduced in [`4.0.2`](https://github.com/lavalink-devs/Lavalink/releases/tag/4.0.2))
* Fixed some issues where plugins would be redownloaded everytime lavalink started (introduced in [`4.0.1`](https://github.com/lavalink-devs/Lavalink/releases/tag/4.0.1))

## v4.0.2
* Fixed issue where all plugins get deleted when already present (introduced in [`v4.0.1`](https://github.com/lavalink-devs/Lavalink/releases/tag/4.0.1))
* Always include plugin info & user data when serializing (introduced in [`v4.0.1`](https://github.com/lavalink-devs/Lavalink/releases/tag/4.0.1))
* Updated oshi to `6.4.11`

## 4.0.1
* Updated Lavaplayer to `2.1.0`
* Updated oshi to `6.4.8`
* Fix/user data missing field exception in protocol
* Fix plugin manager not deleting old plugin version
* Fix not being able to seek when player is paused
* Removed illegal reflection notice

## 4.0.0
* Lavalink now requires Java 17 or higher to run
* **Removal of all websocket messages sent by the client. Everything is now done via [REST](https://lavalink.dev/api/rest.html)**
* Remove default 4GB max heap allocation from docker image
* Removal of all `/v3` endpoints except `/version`. All other endpoints are now under `/v4`
* Reworked track loading result. For more info see [here](https://lavalink.dev/api/rest.md#track-loading-result)
* Update docker ubuntu base image from focal(`20`) to jammy(`22`)
* Update to Koe [`2.0.0-rc2`](https://github.com/KyokoBot/koe/releases/tag/2.0.0-rc2)
* Update Lavaplayer to [`2.0.4`](https://github.com/lavalink-devs/lavaplayer/releases/tag/2.0.4), which includes native support for artwork urls and ISRCs in the track info
* Update to the [Protocol Module](https://github.com/lavalink-devs/Lavalink/tree/master/protocol) to support Kotlin/JS
* Allow setting user data on tracks in the REST API. For more info see [here](https://lavalink.dev/api/rest.html#update-player-track)
* Added default plugin repository. Plugin devs can now request their plugin to be added to the default repository. For more info see [here](https://lavalink.dev/api/plugins.md#distributing-your-plugin)
* Addition of full `Track` objects in following events: `TrackStartEvent`, `TrackEndEvent`, `TrackExceptionEvent`, `TrackStuckEvent`
* Resuming a session now requires the `Session-Id` header instead of `Resume-Key` header
* Add JDA-NAS support for musl (`x86-64`, `aarch64`) based systems (most notably `alpine`)
* Add `Omissible#isPresent` & `Omissible#isOmitted` to the `protocol` module
* New config option to specify the directory to load plugins from. `lavalink.pluginsDir` (defaults to `./plugins`)
* Enable request logging by default
* Fixed error when seeking and player is not playing anything in
* Fixed null pointer when a playlist has no selected track

> [!WARNING]
> Lavalink previously set the `-Xmx` flag to `4G` in docker. This caused issues with some systems which had less than 4GB of RAM. We have now removed this flag and let the JVM decide the max heap allocation. The default is 1GB or 25% of total memory, whichever is lower.
> On how to increase the max heap allocation, see [here](https://lavalink.dev/configuration/docker.html#docker).

<details>
<summary>v4.0.0 - Betas</summary>

## 4.0.0-beta.5
* Update lavaplayer to [`2.0.3`](https://github.com/lavalink-devs/lavaplayer/releases/tag/2.0.2) - Fixed YouTube access token errors
* Added default plugin repository. Plugin devs can now request their plugin to be added to the default repository. For more info see [here](https://github.com/lavalink-devs/Lavalink/blob/master/PLUGINS.md#distributing-your-plugin)
* Fixed error when seeking and player is not playing anything in 

## 4.0.0-beta.4
* Update lavaplayer to [`2.0.2`](https://github.com/lavalink-devs/lavaplayer/releases/tag/2.0.2) - Support MPEG 2.5 and fixed some requests not timing out
* Add `Omissible#isPresent` & `Omissible#isOmitted` to the `protocol` module
* Fix null pointer when a playlist has no selected track

## 4.0.0-beta.3
* Update lavaplayer to [`2.0.0`](https://github.com/lavalink-devs/lavaplayer/releases/tag/2.0.0) - Fixed YouTube 403 errors & YouTube access token errors

## 4.0.0-beta.2
* Update lavaplayer to [`08cfbc0`](https://github.com/Walkyst/lavaplayer-fork/commit/08cfbc05953128f3cf727ea3bcbe41dabcd1c7db) -  Fixed ogg streaming
* Add JDA-NAS support for musl (`x86-64`, `aarch64`) based systems (most notably `alpine`)
* New config option to specify the directory to load plugins from. `lavalink.pluginsDir` (defaults to `./plugins`)

## 4.0.0-beta.1
* New Lavalink now requires Java 17 or higher to run
* **Removal of all websocket messages sent by the client. Everything is now done via [REST](https://lavalink.dev/api/rest.html)**
* Update to [Lavaplayer custom branch](https://github.com/Walkyst/lavaplayer-fork/tree/custom), which includes native support for artwork urls and ISRCs in the track info
* Addition of full `Track` objects in following events: `TrackStartEvent`, `TrackEndEvent`, `TrackExceptionEvent`, `TrackStuckEvent`
* Resuming a session now requires the `Session-Id` header instead of `Resume-Key` header
* Reworked track loading result. For more info see [here](https://lavalink.dev/api/rest.html#track-loading-result)
* Update to the [Protocol Module](protocol) to support Kotlin/JS
* Removal of all `/v3` endpoints except `/version`. All other endpoints are now under `/v4`

> **Warning**
> This is a beta release, and as such, may contain bugs. Please report any bugs you find to the [issue tracker](https://github.com/lavalink-devs/Lavalink/issues/new/choose).
> For more info on the changes in this release, see [here](https://lavalink.dev/changelog/index.html#significant-changes)
> If you have any question regarding the changes in this release, please ask in the [support server](https://discord.gg/ZW4s47Ppw4) or [GitHub discussions](https://github.com/lavalink-devs/Lavalink/discussions/categories/q-a)

Contributors:
[@topi314](https://github.com/topi314), [@freyacodes](https://github.com/freyacodes), [@DRSchlaubi](https://github.com/DRSchlaubi) and [@melike2d](https://github.com/melike2d)

</details>

## v3.7.10
* Update lavaplayer to [`1.5.2`](https://github.com/lavalink-devs/lavaplayer/releases/tag/1.5.2) - Fixed NPE on missing author in playlist tracks in YouTube

## 3.7.9
* Update lavaplayer to [`1.5.1`](https://github.com/lavalink-devs/lavaplayer/releases/tag/1.5.1) - Fixed YouTube access token errors
* Fixed websocket crash when seeking and nothing is playing
* Fixed error when seeking and player is not playing anything

## 3.7.8
* Fix YouTube 403 errors
* Fix YouTube access token errors

## 3.7.7
* Add JDA-NAS support for musl (`x86-64`, `aarch64`) based systems (most notably `alpine`)

## 3.7.6
* Update Lavaplayer to [`1.4.1`](https://github.com/Walkyst/lavaplayer-fork/releases/tag/1.4.1) & [`1.4.2`](https://github.com/Walkyst/lavaplayer-fork/releases/tag/1.4.2)
* New support for `MUSL` based systems (most notably `alpine`)
* New `alpine` docker image variant (use `-alpine` suffix)

## 3.7.5
* Fix `endTime` in `Player Update` endpoint only applying when playing a new track
* Fix errors when doing multiple session resumes
* Update lavaplayer to `1.4.0` see [here](https://github.com/Walkyst/lavaplayer-fork/releases/tag/1.4.0) for more info

> **Note**
> Lavalink Docker images are now found in the GitHub Container Registry instead of DockerHub

## 3.7.4
* Fix an issue where Lavalink would not destroy a session when a client disconnects

## 3.7.3
* Fix breaking change where `/decodetrack` would return a full track instead of the track info

## 3.7.2
* Fix breaking change where frameStats would be null instead of omitted

## 3.7.1
* Revert of application.yml autocreate as it can cause issues with differently named configs

## 3.7.0
* New REST API for player control and deprecation of all websocket OPs. For more info see [here](https://github.com/lavalink-devs/Lavalink/blob/master/IMPLEMENTATION.md#significant-changes-v360---v370)
* Autocreate default `application.yml` if none was found. https://github.com/lavalink-devs/Lavalink/pull/781
* New config option to disable jda nas. https://github.com/lavalink-devs/Lavalink/pull/780
* New config option to disable specific filters. https://github.com/lavalink-devs/Lavalink/pull/779
* Update lavaplayer to `1.3.99.2`. https://github.com/lavalink-devs/Lavalink/pull/794
* Update udpqueue.rs to `v0.2.6`. https://github.com/lavalink-devs/Lavalink/pull/802

Contributors:
[@topi314](https://github.com/topi314), [@Devoxin](https://github.com/Devoxin), [@melike2d](https://github.com/melike2d), [@freyacodes](https://github.com/freyacodes), [@aikaterna](https://github.com/aikaterna), [@ooliver1](https://github.com/ooliver1)

## 3.6.2
* Update lavaplayer to `1.3.99.1`. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/773)

## 3.6.1
* Update lavaplayer to `1.3.99`. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/768)

## 3.6.0
* New userId & clientName getters in the plugin-api. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/743).

Contributors: 
[@melike2d](https://github.com/melike2d)

## 3.5.1
* Update udpqueue.rs to `0.2.5` which fixes crashes when ipv6 is disabled
* Fix null socketContext in `IPlayer` for plugins
* New `ping` field in player update. see https://github.com/lavalink-devs/Lavalink/pull/738 for more info

Contributors: 
[@topi314](https://github.com/topi314), [@Devoxin](https://github.com/Devoxin), and [@freyacodes](https://github.com/freyacodes)

## 3.5
* New plugin system. For more info see [here](https://github.com/lavalink-devs/Lavalink/blob/master/PLUGINS.md).
* Add support for HTTP proxying via httpConfig. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/595).
* Update koe version to 2.0.0-rc1.
  - this fixes the WebSocketClosedEvent with code 1006 problem.
* Fix error when enabling timescale and lowpass filters.
* Fix player not playing after moving between voice chats or changing regions.
* Fix guild ids sent as numbers in json.
* Fix missing timescale natives.
* Fix setting endMarkerHit to correctly set FINISHED as the reason.
* Undeprecation of the `volume` property in the `play` OP.
* Configurable track stuck threshold. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/676).
* Add JDA-NAS support for more CPU Architectures. For more info see [here](https://github.com/lavalink-devs/Lavalink/pull/692). Big thanks goes to @MinnDevelopment here.
* Update lavaplayer to [`1.3.98.4`](https://github.com/Walkyst/lavaplayer-fork/releases/tag/1.3.98.4) which fixes the latest yt cipher issues and age restricted tracks

Contributors: 
[@freyacodes](https://github.com/freyacodes), 
[@davidffa](https://github.com/davidffa), 
[@Walkyst](https://github.com/Walkyst), 
[@topi314](https://github.com/topi314), 
[@duncte123](https://github.com/duncte123), 
[@Kodehawa](https://github.com/Kodehawa), 
[@Devoxin](https://github.com/Devoxin), 
[@Muh9049](https://github.com/Muh9049), 
[@melike2d](https://github.com/melike2d), 
[@ToxicMushroom](https://github.com/ToxicMushroom), 
[@mooner1022](https://github.com/mooner1022), 
[@rohank05](https://github.com/rohank05), 
[@Fabricio20](https://github.com/Fabricio20), 
[@TheEssemm](https://github.com/TheEssemm), and 
[@jack1142](https://github.com/jack1142)

## 3.4
* New filters system
* Deprecation of `TrackExceptionEvent.error`, replaced by `TrackExceptionEvent.exception`
* Added the `connected` boolean to player updates.
* Updated lavaplayer, fixes Soundcloud
* Added source name to REST api track objects
* Clients are now requested to make their name known during handshake

Contributors:
[@freyacodes](https://github.com/freyacodes),
[@duncte123](https://github.com/duncte123),
[@DaliborTrampota](https://github.com/DaliborTrampota),
[@Mandruyd](https://github.com/Mandruyd),
[@Allvaa](https://github.com/@Allvaa), and
[@topi314](https://github.com/topi314)

## 3.3.2.5
* Update Lavaplayer to 1.3.76

## 3.3.2.4
* Update Lavaplayer to 1.3.74

## 3.3.2.3
* Update Lavaplayer to 1.3.65, fixes Soundcloud

## v3.3.2.2
* Updated Lavaplayer to 1.3.61
* Fixed a ConcurrentModificationException ([Thewsomeguy](https://github.com/Thewsomeguy))

## v3.3.2.1
* Updated to Sedmelluq's Lavaplayer 1.3.53

## v3.3.2
* Replaced Magma with Koe.
* Finally implemented `stopTime` for `play` op.
* Added `playerUpdateInterval` config option.
* Added `environment` to Sentry config.
* Fixed #332
* Updated IP rotator.
* Update lavaplayer to `1.3.59` from devoxin's fork.
* Added a Testbot for development.

Contributors:
[@freyacodes](https://github.com/freyacodes),
[@Thewsomeguy](https://github.com/Thewsomeguy),
[@Neuheit](https://github.com/Neuheit),
[@Sangoon_Is_Noob](https://github.com/Sangoon_Is_Noob),
[@TheEssem](https://github.com/Essem), and
[@Devoxin](https://github.com/Devoxin)

## v3.3.1.4
* Update lavaplayer to `1.3.54.3` from devoxin's fork.


## v3.3.1.3
* Update lavaplayer to `1.3.53` from devoxin's fork.

## v3.3.1.2
* Update lavaplayer to [@Devoxin](https://github.com/Devoxin)'s' fork

## v3.3.1.1
* Updated Lavaplayer to `1.3.50`. This notably fixes YouTube search.

Search patch contributed by [@freyacodes](https://github.com/freyacodes)

## v3.3.1
* Update Magma and Lavaplayer.
* Added TrackStartEvent event.
* Added retryLimit configuration option.
* Use a single AudioPlayerManager for all WS connections, reducing overhead.
* Docker images now use Zulu JDK 13 to mitigate TLS 1.3 problems.

Contributors:
[@freyacodes](https://github.com/freyacodes),
[@duncte123](https://github.com/duncte123),
[@ByteAlex](https://github.com/ByteAlex), and
[@Xavinlol](https://github.com/Xavinlol)

## v3.3

Officially limit Lavalink to JRE 11 and up. Magma has long been having issues with older versions.

## v3.2.2
* IP rotation system for getting around certain ratelimits.
* Update Lavaplayer to 1.3.32.
* Docker container now uses a non-root user.

Contributors:
[@freyacodes](https://github.com/freyacodes),
[@ByteAlex](https://github.com/ByteAlex),
[@duncte123](https://github.com/duncte123), and
[@james7132](https://github.com/james7132)

## v3.2.1.1
* Updated Lavaplayer to 1.3.19. This release includes a patch which fixes loading youtube URLs. 
https://github.com/sedmelluq/lavaplayer/pull/199
* Made the WebSocket handshake return code 401 instead of 200 on bad auth. #208

Contributors:
[@freyacodes](https://github.com/freyacodes) and
[@Devoxin](https://github.com/Devoxin)



## v3.2.1
* Update dependencies -- fixes frequent youtube HTTP errors
* Return `FriendlyException` message on `LOAD_FAILED` #174
* Add option to disable `ytsearch` and `scsearch` #194

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@duncte123](https://github.com/duncte123),
[@freyacodes](https://github.com/freyacodes), and
[@napstr](https://github.com/napstr)

## v3.2.0.3
* Add compatibility for Java 8-10

Contributor:
[@MinnDevelopment](https://github.com/MinnDevelopment/)

## v3.2.0.2
* Patched magma

Contributor:
[@freyacodes](https://github.com/freyacodes/)

## v3.2.0.1
* Bumped to Java 11. Treating this as a patch version, as v3.2 still requires Java 11 due to a Magma update. 

[@freyacodes](https://github.com/freyacodes)

## v3.2
* Added support for resuming
* Added noReplace option to the play op
* Sending the same voice server update will not cause an existing connection to reconnect

Contributor:
[@freyacodes](https://github.com/freyacodes)

## v3.1.2
* Add API version header to all responses

Contributor:
[@Devoxin](https://github.com/Devoxin)

## v3.1.1
* Add equalizer support
* Update lavaplayer to 1.3.10
* Fixed automatic versioning
* Added build config to upload binaries to GitHub releases from CI

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@calebj](https://github.com/calebj)

## v3.1
* Replaced JDAA with Magma
* Added an event for when the Discord voice WebSocket is closed
* Replaced Tomcat and Java_Websocket with Undertow. WS and REST is now handled by the same 
server and port. Port is specified by `server.port`.

## v3.0
* **Breaking:** The minimum required Java version to run the server is now Java 10.   
**Please note**: Java 10 will be obsolete
as of [September 2018 with the release of Java 11](http://www.java-countdown.xyz/). Expect a Lavalink major version release that will be targetting
Java 11 by that time.
* **Breaking:** Changes to the output of the /loadtracks endpoint. [\#91](https://github.com/lavalink-devs/Lavalink/pull/91), [\#114](https://github.com/lavalink-devs/Lavalink/pull/114), [\#116](https://github.com/lavalink-devs/Lavalink/pull/116)
* **Breaking:** The Java client has been moved to a [new repository](https://github.com/lavalink-devs/Lavalink-Client).
* **Breaking:** The Java client has been made generic. This is a breaking change so please read the [migration guide](https://github.com/lavalink-devs/Lavalink-Client#migrating-from-v2-to-v3).
* Better configurable logging. [\#97](https://github.com/lavalink-devs/Lavalink/pull/97)
* Add custom sentry tags, change sentry dsn configuration location. [\#103](https://github.com/lavalink-devs/Lavalink/pull/103)
* Add Lavalink version header to websocket handshake. [\#111](https://github.com/lavalink-devs/Lavalink/pull/111)
* Use git tags for easier version visibility. [\#129](https://github.com/lavalink-devs/Lavalink/pull/129)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr),
[@SamOphis](https://github.com/SamOphis)

## v2.2
* Lavaplayer updated to 1.3.x [\#115](https://github.com/lavalink-devs/Lavalink/pull/115)
* Version command line flag [\#121](https://github.com/lavalink-devs/Lavalink/pull/121)
* Fix race condition in `/loadtracks` endpoint leading to some requests never completing [\#125](https://github.com/lavalink-devs/Lavalink/pull/125)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr)

## v2.1
* Add prometheus metrics [\#105](https://github.com/lavalink-devs/Lavalink/pull/105), [\#106](https://github.com/lavalink-devs/Lavalink/pull/106)

Contributors:
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0.1
* Configurable playlist load limit [\#60](https://github.com/lavalink-devs/Lavalink/pull/60)
* [Docker Releases](https://hub.docker.com/r/fredboat/lavalink/), [\#74](https://github.com/lavalink-devs/Lavalink/pull/74)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@itslukej](https://github.com/itslukej/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0
Please see [here](https://github.com/lavalink-devs/Lavalink/commit/b8dd3c8a7e186755c1ab343d19a552baecf138e7)
and [here](https://github.com/lavalink-devs/Lavalink/commit/08a34c99a47a18ade7bd14e6c55ab92348caaa88)
