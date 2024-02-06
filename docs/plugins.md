---
description: A list of plugins for Lavalink.
---

# Plugins

Lavalink supports third-party plugins to add additional functionality such as custom audio sources, custom filters,
WebSocket handling, REST endpoints, and much more.

Lavalink loads all .jar files placed in the `plugins` directory, which you may need to create yourself. Lavalink can
also download plugin .jar files automatically by editing the configuration file. See the respective plugin repository
for instructions.

| Plugin                                                                    | Description                                                                                            |
|---------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| [Google Cloud TTS plugin](https://github.com/DuncteBot/tts-plugin)        | A text to speech plugin using the [google cloud tts api](https://cloud.google.com/text-to-speech/docs) |
| [SponsorBlock plugin](https://github.com/topi314/Sponsorblock-Plugin)     | Skip sponsor segments in YouTube videos & return YouTube video chapter information                     |
| [LavaSrc plugin](https://github.com/topi314/LavaSrc)                      | Spotify, Apple Music & Deezer(native play) support                                                     |
| [LavaSearch plugin](https://github.com/topi314/LavaSearch)                | Advanced search functionality including playlists, albums, artists, tracks & terms                     |
| [DuncteBot plugin](https://github.com/DuncteBot/skybot-lavalink-plugin)   | Additional source managers that are not widely used                                                    |
| [XM plugin](https://github.com/esmBot/lava-xm-plugin)                     | Support for various [music tracker module](https://en.wikipedia.org/wiki/Module_file) formats          |
| [Lyrics.kt plugin](https://github.com/DRSchlaubi/lyrics.kt)               | Plugin that fetches timestamped lyrics from YouTube                                                    |
| [Java Timed Lyrics](https://github.com/DuncteBot/java-timed-lyrics)       | Timestamped lyrics from YouTube with Genius fallback, supports IP-rotation                             |
| [LavaDSPX Audio Filters](https://github.com/Devoxin/LavaDSPX-Plugin)      | Additional audio filters for Lavalink                                                                  |

If you want to make your own plugin see [here](api/plugins.md)
