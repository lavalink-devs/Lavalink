# Changelog

Each release usually includes various fixes and improvements.
The most noteworthy of these, as well as any features and breaking changes, are listed here.

## v3.0
* **Breaking:** The minimum required Java version to run the server is now Java 10.   
**Please note**: Java 10 will be obsolete
as of [September 2018 with the release of Java 11](http://www.java-countdown.xyz/). Expect a Lavalink major version release that will be targetting
Java 11 by that time.
* **Breaking:** Changes to the output of the /loadtracks endpoint. [\#91](https://github.com/Frederikam/Lavalink/pull/91), [\#114](https://github.com/Frederikam/Lavalink/pull/114), [\#116](https://github.com/Frederikam/Lavalink/pull/116)
* **Breaking:** The Java client has been moved to a [new repository](https://github.com/FredBoat/Lavalink-Client).
* **Breaking:** The Java client has been made generic. This is a breaking change so please read the [migration guide](https://github.com/FredBoat/Lavalink-Client#migrating-from-v2-to-v3).
* Better configurable logging. [\#97](https://github.com/Frederikam/Lavalink/pull/97)
* Add custom sentry tags, change sentry dsn configuration location. [\#103](https://github.com/Frederikam/Lavalink/pull/103)
* Add Lavalink version header to websocket handshake. [\#111](https://github.com/Frederikam/Lavalink/pull/111)
* Use git tags for easier version visibility. [\#129](https://github.com/Frederikam/Lavalink/pull/129)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@Frederikam](https://github.com/Frederikam/),
[@napstr](https://github.com/napstr),
[@SamOphis](https://github.com/SamOphis)

## v2.2
* Lavaplayer updated to 1.3.x [\#115](https://github.com/Frederikam/Lavalink/pull/115)
* Version command line flag [\#121](https://github.com/Frederikam/Lavalink/pull/121)
* Fix race condition in `/loadtracks` endpoint leading to some requests never completing [\#125](https://github.com/Frederikam/Lavalink/pull/125)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@Frederikam](https://github.com/Frederikam/),
[@napstr](https://github.com/napstr)

## v2.1
* Add prometheus metrics [\#105](https://github.com/Frederikam/Lavalink/pull/105), [\#106](https://github.com/Frederikam/Lavalink/pull/106)

Contributors:
[@Frederikam](https://github.com/Frederikam/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0.1
* Configurable playlist load limit [\#60](https://github.com/Frederikam/Lavalink/pull/60)
* [Docker Releases](https://hub.docker.com/r/fredboat/lavalink/), [\#74](https://github.com/Frederikam/Lavalink/pull/74)

Contributors:
[@Devoxin](https://github.com/Devoxin),
[@Frederikam](https://github.com/Frederikam/),
[@itslukej](https://github.com/itslukej/),
[@napstr](https://github.com/napstr),
[@Repulser](https://github.com/Repulser/)

## v2.0
Please see [here](https://github.com/Frederikam/Lavalink/commit/b8dd3c8a7e186755c1ab343d19a552baecf138e7)
and [here](https://github.com/Frederikam/Lavalink/commit/08a34c99a47a18ade7bd14e6c55ab92348caaa88)
