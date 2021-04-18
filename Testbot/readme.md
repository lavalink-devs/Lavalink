# Lavalink Testbot
This is a minimalistic example of a bot using Lavalink. This example is based upon Lavalink-Client for Java.

https://github.com/freyacodes/lavalink-client

This uses Lavaplayer to load tracks rather than initially loading tracks via Lavalink. Non-JVM (Java) bots will need
to query the `/loadtracks` endpoint.


## Running the test bot
This guide assumes the following:
* You have a Lavalink node already running
* You have a Discord bot and its token
* You have a shell in the same directory as this readme
* You have Java 11 or newer. Check with `java -version`

Run this command. Gradle will download as needed, and will build and run the bot:
```bash
../gradlew run --args "YOUR_BOT_TOKEN ws://localhost:2333 youshallnotpass"
```

Replace token, host, and password as needed.

Replace `../gradlew` with `../gradlew.bat` if on Windows.


## Using the test bot
Only one command is currently supported, and only the first or selected track of a playlist or search will be played. Example usages:
```
;;play https://www.youtube.com/watch?v=cRh1-_pRDzo
;;play https://soundcloud.com/home-2001/resonance
;;play https://www.youtube.com/watch?v=t2D5HlKLh34&list=PLKUyqLlH6brkzzJgD6Gdriga4mdtCAMBJ
;;play ytsearch: John Coltrane Giant Steps
;;play https://www.youtube.com/watch?v=dQw4w9WgXcQ
```