---
description: A list of Lavalink client libraries.
---

# Client Libraries

| Client                                                              | Platform        | Compatible With                            | DAVE Support | Additional Information             |
|---------------------------------------------------------------------|-----------------|--------------------------------------------|--------------|------------------------------------|
| [Lavalink-Client](https://github.com/lavalink-devs/Lavalink-Client) | Java/Kotlin/JVM | JDA/Discord4J/**Any**                      | ✅            | Uses reactor                       |
| [Lavalink.kt](https://github.com/DRSchlaubi/Lavalink.kt)            | Kotlin          | Kord/JDA/**Any**                           | ✅            | Kotlin Coroutines                  |
| [DisGoLink](https://github.com/disgoorg/disgolink)                  | Go              | **Any**                                    | ✅            |                                    |
| [lavalink.py](https://github.com/devoxin/lavalink.py)               | Python          | **Any**                                    | ✅            |                                    |
| [Mafic](https://github.com/ooliver1/mafic)                          | Python          | discord.py **V2**/nextcord/disnake/py-cord | ✅            |                                    |
| [Pomice](https://github.com/cloudwithax/pomice)                     | Python          | discord.py **V2**                          | ✅            |                                    |
| [Wavelink](https://github.com/PythonistaGuild/Wavelink)             | Python          | discord.py **V2**                          | ✅            |                                    |
| [hikari-ongaku](https://github.com/MPlatypus/hikari-ongaku)         | Python          | Hikari                                     | ✅            | `asyncio`-based                    |
| [lavaplay.py](https://github.com/HazemMeqdad/lavaplay.py)           | Python          | **Any**                                    | ✅            | `asyncio`-based libraries 1.0.13a+ |
| [Moonlink.js](https://github.com/1Lucas1apk/moonlink.js)            | Node.js         | **Any**                                    | ✅            |                                    |
| [Magmastream](https://gitryx.com/MagmaStream/magmastream)           | Node.js         | **Any**                                    | ✅            |                                    |
| [Shoukaku](https://github.com/Deivu/Shoukaku)                       | Node.js         | **Any**                                    | ✅            |                                    |
| [Lavalink-Client](https://github.com/tomato6966/Lavalink-Client)    | Node.js         | discord.js/DiscordDeno/Eris/**Any**        | ✅            | `async`                            |
| [FastLink](https://github.com/PerformanC/FastLink)                  | Node.js         | **Any**                                    | ✅            |                                    |
| [Riffy](https://github.com/riffy-team/riffy)                        | Node.js         | **Any**                                    | ✅            |                                    |
| [lavaclient](https://github.com/lavaclient/lavaclient)              | Node.js         | **Any**                                    | ✅            | v5+                                |
| [Rainlink](https://github.com/RainyXeon/Rainlink)                   | Node.js         | **Any**                                    | ✅            |                                    |
| [DisCatSharp](https://github.com/Aiko-IT-Systems/DisCatSharp)       | .NET            | DisCatSharp                                | ✅            | v10.7.0+                           |
| [Lavalink4NET](https://github.com/angelobreuer/Lavalink4NET)        | .NET            | Discord.Net/DSharpPlus/Remora/NetCord      | ✅            | v4+                                |
| [Coglink](https://github.com/PerformanC/Coglink)                    | C               | Concord                                    | ✅            |                                    |
| [Anchorage](https://github.com/Deivu/Anchorage)                     | Rust            | **Any**                                    | ✅            | `tokio`-based                      |
| [lavalink-rs](https://gitlab.com/vicky5124/lavalink-rs)             | Rust, Python    | **Any**                                    | ✅            | `tokio`-based, `asyncio`-based     |
| [nyxx_lavalink](https://github.com/nyxx-discord/nyxx_lavalink)      | Dart            | nyxx/**Any**                               | ✅            |                                    |

<details markdown="1">
<summary>Not DAVE supporting Client Libraries</summary>

| Client                                               | Platform           | Compatible With           | DAVE Support | Additional Information |
|------------------------------------------------------|--------------------|---------------------------|--------------|------------------------|
| [Lavacord](https://github.com/lavacord/Lavacord)     | Node.js/TypeScript | **Any**                   | ❌            | `Unmaintained`         |
| [TsumiLink](https://github.com/Fyphen1223/TsumiLink) | Node.js            | **Any**                   | ❌            | `Unmaintained`         |
| [Blue.ts](https://github.com/ftrapture/blue.ts)      | Node.js            | Discord.js/Eris/OceanicJs | ❌            | `Unmaintained`         |
| [Nomia](https://github.com/DHCPCD9/Nomia)            | .NET               | DSharpPlus                | ❌            | `Unmaintained`         |

</details>

Or alternatively, you can create your own client library, following the [implementation documentation](api/index.md).
Any client libraries marked with `Unmaintained` have been marked as such as their repositories have not received any commits for at least 1 year since time of checking,
however they are listed as they may still support Lavalink, and/or have not needed maintenance, however keep in mind that compatibility and full feature support is not guaranteed.
