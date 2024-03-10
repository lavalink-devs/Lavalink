---
description: A list of Lavalink client libraries.
---

# Client Libraries

| Client                                                              | Platform        | Compatible With                            | Additional Information         |
|---------------------------------------------------------------------|-----------------|--------------------------------------------|--------------------------------|
| [Lavalink-Client](https://github.com/lavalink-devs/Lavalink-Client) | Java/Kotlin/JVM | JDA/Discord4J/**Any**                      | Uses reactor                   |
| [Lavalink.kt](https://github.com/DRSchlaubi/Lavalink.kt)            | Kotlin          | Kord/JDA/**Any**                           | Kotlin Coroutines              |
| [DisGoLink](https://github.com/disgoorg/disgolink)                  | Go              | **Any**                                    |                                |
| [lavalink.py](https://github.com/devoxin/lavalink.py)               | Python          | **Any**                                    |                                |
| [Mafic](https://github.com/ooliver1/mafic)                          | Python          | discord.py **V2**/nextcord/disnake/py-cord |                                |
| [Wavelink](https://github.com/PythonistaGuild/Wavelink)             | Python          | discord.py **V2**                          |                                |
| [Pomice](https://github.com/cloudwithax/pomice)                     | Python          | discord.py **V2**                          |                                |
| [hikari-ongaku](https://github.com/MPlatypus/hikari-ongaku)         | Python          | Hikari                                     | `asyncio`-based                |
| [Moonlink.js](https://github.com/1Lucas1apk/moonlink.js)            | Node.js         | **Any**                                    |                                |
| [Magmastream](https://github.com/Blackfort-Hosting/magmastream)     | Node.js         | **Any**                                    |                                |
| [Lavacord](https://github.com/lavacord/Lavacord)                    | Node.js         | **Any**                                    |                                |
| [Shoukaku](https://github.com/Deivu/Shoukaku)                       | Node.js         | **Any**                                    |                                |
| [Lavalink-Client](https://github.com/tomato6966/Lavalink-Client)    | Node.js         | **Any**                                    |                                |
| [FastLink](https://github.com/PerformanC/FastLink)                  | Node.js         | **Any**                                    |                                |
| [Riffy](https://github.com/riffy-team/riffy)                        | Node.js         | **Any**                                    |                                |
| [DisCatSharp](https://github.com/Aiko-IT-Systems/DisCatSharp)       | .NET            | DisCatSharp                                | v10.4.2+                       |
| [Lavalink4NET](https://github.com/angelobreuer/Lavalink4NET)        | .NET            | Discord.Net/DSharpPlus/Remora/NetCord      | v4+                            |
| [Nomia](https://github.com/DHCPCD9/Nomia)                           | .NET            | DSharpPlus                                 |                                |
| [Coglink](https://github.com/PerformanC/Coglink)                    | C               | Concord                                    |                                |
| [lavalink-rs](https://gitlab.com/vicky5124/lavalink-rs)             | Rust, Python    | **Any**                                    | `tokio`-based, `asyncio`-based |
| [lavalink](https://github.com/nyxx-discord/nyxx_lavalink)           | Dart            | nyxx/**Any**                               |                                |

<details markdown="1">
<summary>v3.7 supporting Client Libraries</summary>

| Client                                                        | Platform | Compatible With                            | Additional Information          |
|---------------------------------------------------------------|----------|--------------------------------------------|---------------------------------|
| [Lavalink.kt](https://github.com/DRSchlaubi/lavalink.kt)      | Kotlin   | JDA/Kord/**Any**                           | Kotlin Coroutines               |
| [lavaplay.py](https://github.com/HazemMeqdad/lavaplay.py)     | Python   | **Any\***                                  | *`asyncio`-based libraries only |
| [Mafic](https://github.com/ooliver1/mafic)                    | Python   | discord.py **V2**/nextcord/disnake/py-cord |                                 |
| [Wavelink](https://github.com/PythonistaGuild/Wavelink)       | Python   | discord.py **V2**                          | Version >=2, <3                 |
| [Pomice](https://github.com/cloudwithax/pomice)               | Python   | discord.py **V2**                          |                                 |
| [Lavacord](https://github.com/lavacord/lavacord)              | Node.js  | **Any**                                    |                                 |
| [Poru](https://github.com/parasop/poru)                       | Node.js  | **Any**                                    |                                 |
| [Shoukaku](https://github.com/Deivu/Shoukaku)                 | Node.js  | **Any**                                    |                                 |
| [Cosmicord.js](https://github.com/SudhanPlayz/Cosmicord.js)   | Node.js  | **Any**                                    |                                 |
| [DisCatSharp](https://github.com/Aiko-IT-Systems/DisCatSharp) | .NET     | DisCatSharp                                | Only prior v10.4.1              |
| [Lavalink4NET](https://github.com/angelobreuer/Lavalink4NET)  | .NET     | Discord.Net/DSharpPlus                     | < v4                            |
| [DisGoLink](https://github.com/disgoorg/disgolink)            | Go       | **Any**                                    |                                 |

</details>

Or alternatively, you can create your own client library, following the [implementation documentation](api/index.md).
Any client libraries marked with `Unmaintained` have been marked as such as their repositories have not received any commits for at least 1 year since time of checking,
however they are listed as they may still support Lavalink, and/or have not needed maintenance, however keep in mind that compatibility and full feature support is not guaranteed.
