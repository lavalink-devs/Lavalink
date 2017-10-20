# Lavalink JDA Client
## Installation
Lavalink does not have a maven repository and instead uses Jitpack.
You can add the following to your POM if you're using Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.github.Frederikam</groupId>
        <artifactId>Lavalink</artifactId>
        <version>version-goes-here</version>
        <exclusions>
            <exclusion>
                <!-- Exclude the server module -->
                <groupId>com.github.Frederikam.Lavalink</groupId>
                <artifactId>Lavalink-Server</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>
```

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Jitpack versions
Jitpack versioning is based on git branches and commit hashes. Eg:

```
abfb66cc25f5f121e5441726ad70095aee161c71
master-SNAPSHOT
dev-SNAPSHOT
```

## Usage
[FredBoat](https://github.com/Frederikam/FredBoat) is a great reference.

### Configuring Lavalink
All your shards should share a single Lavalink instance. Here is how to construct an instance:

```java
Lavalink lavalink = new Lavalink(
                myDiscordUserId,
                fixedNumberOfShards,
                shardId -> getJdaInstanceFromId(shardId)
        );
```

The interesting part is the third parameter, which is a `Function<Integer, JDA>`.
You must define this `Function` so that Lavalink can get your current JDA instance for that shardId.

You can now register remote nodes to your Lavalink instance:
```java
lavalink.addNode("ws://example.com", "my-secret-password");
```

If a node is down Lavalink will continue trying to connect until you remove the node.
When a node dies Lavalink will attempt to balance the load unto other nodes if they are available.

Next when you are building a shard, you must register Lavalink as an event listener to bind your shard.
You may not register more than one Lavalink instance per shard.

```java
new JDABuilder(AccountType.BOT)
        .addEventListener(LavalinkManager.ins.getLavalink())
        ...
```

### The Link class
The `Link` class is the state of one of your guilds in relation to Lavalink.
A `Link` object is instantiated if it doesn't exist already when invoking `Lavalink#getLink(Guild/String)`.

```java
Link someLink = myLavalink.getLink(someGuild);
someLink = myLavalink.getLink(someGuildId);
```

Here are a few important methods:
* `connect(VoiceChannel channel)` connects you to a VoiceChannel.
  * Note: This also works for moving to a new channel, in which case we will disconnect first.
* `disconnect()` disconnects from the VoiceChannel.
* `destroy()` resets the state of the `Link` and removes Lavalink's internal reference to this Link. This `Link` should be discarded.
* `getPlayer()` returns an `IPlayer` you can use to play music with.

The `IPlayer` more or less works like a drop-in replacement for Lavaplayer's `AudioPlayer`. Which leads me to...

**Warning:** You should not use JDA's `AudioManager#openAudioConnection()` or `AudioManager#closeAudioConnection()` when using Lavalink is being used. Use `Link` instead.

### Using Lavalink and Lavaplayer in the same codebase
One of the requirements for Lavalink to work with FredBoat was to make Lavalink optional, so we can support selfhosters who do not want to run Lavalink.
In FredBoat this is accomplished with the [LavalinkManager](https://github.com/Frederikam/FredBoat/blob/master/FredBoat/src/main/java/fredboat/audio/player/LavalinkManager.java) class.

Lavalink-Client adds an abstraction layer:
* `IPlayer` in place of `AudioPlayer`
* `IPlayerEventListener` in place of `AudioEventListener`
* `PlayerEventListenerAdapter` in place of `AudioEventAdapter`

What this means is that if you want to use Lavaplayer directly instead, you can still use `IPlayer`.
```java
IPlayer myNewPlayer = isLavalinkEnabled
        ? lavalink.getLink(guildId).getPlayer()
        : new LavaplayerPlayerWrapper(myLavaplayerPlayerManager.createPlayer());
```

### Node statistics
Lavalink-Client allows access to the client WebSockets with `Lavalink#getNodes()`.
This is useful if you want to read the statistics and state of a node connection.

Useful methods:
* `isAvailable()` Whether or not we are connected and can play music.
* `getRemoteUri()` Returns a `URI` of the remote address.
* `getStats()` Returns a nullable `RemoteStats` object, with statistics from the Lavalink Server. Updated every minute.
