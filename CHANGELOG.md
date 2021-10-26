# Changelog

Each release usually includes various fixes and improvements.
The most noteworthy of these, as well as any features and breaking changes, are listed here.

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
[@TopiSenpai](https://github.com/TopiSenpai)

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
* **Breaking:** Changes to the output of the /loadtracks endpoint. [\#91](https://github.com/freyacodes/Lavalink/pull/91), [\#114](https://github.com/freyacodes/Lavalink/pull/114), [\#116](https://github.com/freyacodes/Lavalink/pull/116)
* **Breaking:** The Java client has been moved to a [new repository](https://github.com/freyacodes/Lavalink-Client).
* **Breaking:** The Java client has been made generic. This is a breaking change so please read the [migration guide](https://github.com/freyacodes/Lavalink-Client#migrating-from-v2-to-v3).
* Better configurable logging. [\#97](https://github.com/freyacodes/Lavalink/pull/97)
* Add custom sentry tags, change sentry dsn configuration location. [\#103](https://github.com/freyacodes/Lavalink/pull/103)
* Add Lavalink version header to websocket handshake. [\#111](https://github.com/freyacodes/Lavalink/pull/111)
* Use git tags for easier version visibility. [\#129](https://github.com/freyacodes/Lavalink/pull/129)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr),
[@SamOphis](https://github.com/SamOphis)

## v2.2
* Lavaplayer updated to 1.3.x [\#115](https://github.com/freyacodes/Lavalink/pull/115)
* Version command line flag [\#121](https://github.com/freyacodes/Lavalink/pull/121)
* Fix race condition in `/loadtracks` endpoint leading to some requests never completing [\#125](https://github.com/freyacodes/Lavalink/pull/125)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr)

## v2.1
* Add prometheus metrics [\#105](https://github.com/freyacodes/Lavalink/pull/105), [\#106](https://github.com/freyacodes/Lavalink/pull/106)

Contributors:
[@freyacodes](https://github.com/freyacodes/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0.1
* Configurable playlist load limit [\#60](https://github.com/freyacodes/Lavalink/pull/60)
* [Docker Releases](https://hub.docker.com/r/fredboat/lavalink/), [\#74](https://github.com/freyacodes/Lavalink/pull/74)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@freyacodes](https://github.com/freyacodes/),
[@itslukej](https://github.com/itslukej/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0
Please see [here](https://github.com/freyacodes/Lavalink/commit/b8dd3c8a7e186755c1ab343d19a552baecf138e7)
and [here](https://github.com/freyacodes/Lavalink/commit/08a34c99a47a18ade7bd14e6c55ab92348caaa88)
