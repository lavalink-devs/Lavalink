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
| [Extra Filter plugin](https://github.com/rohank05/lavalink-filter-plugin) | Additional audio filters for lavalink                                                                  |
| [XM plugin](https://github.com/esmBot/lava-xm-plugin)                     | Support for various [music tracker module](https://en.wikipedia.org/wiki/Module_file) formats          |

If you want to make your own plugin see [here](api/plugins.md)