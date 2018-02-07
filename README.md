# Lavalink
Standalone audio sending node based on Lavaplayer and JDA-Audio.
Allows for sending audio without it ever reaching any of your shards.

Being used in production by FredBoat, Dyno, Rythm, LewdBot, and more.

## Features
* Powered by Lavaplayer
* Minimal CPU/memory footprint
* Twitch/YouTube stream support
* Event system
* Seeking
* Volume control
* REST API for resolving lavaplayer tracks (used for non-JVM clients)
* Statistics (good for load balancing)
* Basic authentication

## Client libraries:
### Supports 2.x:
* [JDA client](https://github.com/Frederikam/Lavalink/tree/master/LavalinkClient) (JDA, Java)
* [Lavalink.py](https://github.com/Devoxin/Lavalink.py) (discord.py, Python)
* [pylava](https://github.com/Pandentia/pylava) (discord.py, Python)
* Or [create your own](https://github.com/Frederikam/Lavalink/blob/master/IMPLEMENTATION.md)

### Supports 1.x:
* [eris-lavalink](https://github.com/briantanner/eris-lavalink) (Eris, JavaScript)
* [lava-d.js](https://github.com/untocodes/lava-d.js) (discord.js, JavaScript)
* [lavalink.js](https://github.com/briantanner/lavalink.js) (discord.js, JavaScript)
* [SandySounds](https://github.com/MrJohnCoder/SandySounds) (JavaScript)

Outdated as of January 29 2018.

## Server configuration
Download from [the CI server](https://ci.fredboat.com/viewLog.html?buildId=lastSuccessful&buildTypeId=Lavalink_Build&tab=artifacts&guest=1)

Put an `application.yml` file in your working directory. [Example](https://github.com/Frederikam/Lavalink/blob/master/LavalinkServer/application.yml.example)

Run with `java -jar Lavalink.jar`

# Acknowledgements
This project contains modified code from https://github.com/sedmelluq/jda-nas v1.0.5
