# Implementation guidelines
How to write your own client. The Java [Lavalink-Client](https://github.com/FredBoat/Lavalink-Client) will serve as an example implementation.
The Java client has support for JDA, but can also be adapted to work with other JVM libraries.

## Requirements
* You must be able to send messages via a shard's mainWS connection.
* You must be able to intercept voice server updates from mainWS on your shard connection.
* One of the following WS drafts (all but RFC 6455 is deprecated but should work):
    * RFC 6455
    * Hybi 17
    * Hybi 10
    * Hixie 76
    * Hixie 75

## Significant changes v2.0 -> v3.0 
* The response of `/loadtracks` has been completely changed (again since the initial v3.0 pre-release).
* Lavalink v3.0 now reports its version as a handshake response header.
`Lavalink-Major-Version` has a value of `3` for v3.0 only. It's missing for any older version.

## Significant changes v1.3 -> v2.0 
With the release of v2.0 many unnecessary ops were removed:

* `connect`
* `disconnect`
* `validationRes`
* `isConnectedRes`
* `validationReq`
* `isConnectedReq`
* `sendWS`

With Lavalink 1.x the server had the responsibility of handling Discord VOICE_SERVER_UPDATEs as well as its own internal ratelimiting.
This remote handling makes things unnecessarily complicated and adds a lot og points where things could go wrong. 
One problem we noticed is that since JDAA is unaware of ratelimits on the bot's gateway connection, it would keep adding
to the ratelimit queue to the gateway. With this update this is now the responsibility of the Lavalink client or the 
Discord client.

A voice connection is now initiated by forwarding a `voiceUpdate` (VOICE_SERVER_UPDATE) to the server. When you want to
disconnect or move to a different voice channel you must send Discord a new VOICE_STATE_UPDATE. If you want to move your
connection to a new Lavalink server you can simply send the VOICE_SERVER_UPDATE to the new node, and the other node
will be disconnected by Discord.

Depending on your Discord library, it may be possible to take advantage of the library's OP 4 handling. For instance,
the JDA client takes advantage of JDA's websocket write thread to send OP 4s for connects, disconnects and reconnects.

## Protocol
### Opening a connection
When opening a websocket connection, you must supply 3 required headers:
```
Authorization: Password matching the server config
Num-Shards: Total number of shards your bot is operating on
User-Id: The user id of the bot you are playing music with
```

### Outgoing messages
Provide an intercepted voice server update. This causes the server to connect to the voice channel
```json
{
    "op": "voiceUpdate",
    "guildId": "...",
    "sessionId": "...",
    "event": "..."
}
```

Cause the player to play a track.
`startTime` is an optional setting that determines the number of milliseconds to offset the track by. Defaults to 0.
`endTime` is an optional setting that determines at the number of milliseconds at which point the track should stop playing. Helpful if you only want to play a snippet of a bigger track. By default the track plays until it's end as per the encoded data.
```json
{
    "op": "play",
    "guildId": "...",
    "track": "...",
    "startTime": "60000",
    "endTime": "120000"
}
```

Cause the player to stop
```json
{
    "op": "stop",
    "guildId": "..."
}
```

Set player pause
```json
{
    "op": "pause",
    "guildId": "...",
    "pause": true
}
```

Make the player seek to a position of the track. Position is in millis
```json
{
    "op": "seek",
    "guildId": "...",
    "position": 60000
}
```

Set player volume. Volume may range from 0 to 1000. 100 is default.
```json
{
    "op": "volume",
    "guildId": "...",
    "volume": 125
}
```

Tell the server to potentially disconnect from the voice server and potentially remove the player with all its data.
This is useful if you want to move to a new node for a voice connection. Calling this op does not affect voice state,
and you can send the same VOICE_SERVER_UPDATE to a new node.
```json
{
    "op": "destroy",
    "guildId": "..."
}
```

### Incoming messages
See 
[LavalinkSocket.java](https://github.com/FredBoat/Lavalink-Client/blob/master/src/main/java/lavalink/client/io/LavalinkSocket.java)
for client implementation

Position information about a player. Includes unix timestamp.
```json
{
    "op": "playerUpdate",
    "guildId": "...",
    "state": {
        "time": 1500467109,
        "position": 60000
    }
}
```

A collection of stats sent every minute. 
```json
{
    "op": "stats",
    ...
}
```

Example implementation of stats:
```java
players = json.getInt("players");
playingPlayers = json.getInt("playingPlayers");
uptime = json.getLong("uptime");

memFree = json.getJSONObject("memory").getInt("free");
memUsed = json.getJSONObject("memory").getInt("used");
memAllocated = json.getJSONObject("memory").getInt("allocated");
memReservable = json.getJSONObject("memory").getInt("reservable");

cpuCores = json.getJSONObject("cpu").getInt("cores");
systemLoad = json.getJSONObject("cpu").getDouble("systemLoad");
lavalinkLoad = json.getJSONObject("cpu").getDouble("lavalinkLoad");

JSONObject frames = json.optJSONObject("frameStats");

if (frames != null) {
    avgFramesSentPerMinute = frames.getInt("sent");
    avgFramesNulledPerMinute = frames.getInt("nulled");
    avgFramesDeficitPerMinute = frames.getInt("deficit");
}
```

Server emitted an event. See the client implementation below.
```json
{
    "op": "event",
    ...
}
```

```java
/**
 * Implementation details:
 * The only events extending {@link lavalink.client.player.event.PlayerEvent} produced by the remote server are these:
 * 1. TrackEndEvent
 * 2. TrackExceptionEvent
 * 3. TrackStuckEvent
 * <p>
 * The remaining are caused by the client
 */
private void handleEvent(JSONObject json) throws IOException {
    LavalinkPlayer player = (LavalinkPlayer) lavalink.getPlayer(json.getString("guildId"));
    PlayerEvent event = null;

    switch (json.getString("type")) {
        case "TrackEndEvent":
            event = new TrackEndEvent(player,
                    LavalinkUtil.toAudioTrack(json.getString("track")),
                    AudioTrackEndReason.valueOf(json.getString("reason"))
            );
            break;
        case "TrackExceptionEvent":
            event = new TrackExceptionEvent(player,
                    LavalinkUtil.toAudioTrack(json.getString("track")),
                    new RemoteTrackException(json.getString("error"))
            );
            break;
        case "TrackStuckEvent":
            event = new TrackStuckEvent(player,
                    LavalinkUtil.toAudioTrack(json.getString("track")),
                    json.getLong("thresholdMs")
            );
            break;
        default:
            log.warn("Unexpected event type: " + json.getString("type"));
            break;
    }

    if (event != null) player.emitEvent(event);
}
```

See also: [AudioTrackEndReason.java](https://github.com/sedmelluq/lavaplayer/blob/master/main/src/main/java/com/sedmelluq/discord/lavaplayer/track/AudioTrackEndReason.java)

### REST API
The REST api is used to resolve audio tracks for use with the `play` op. 
```
GET /loadtracks?identifier=dQw4w9WgXcQ HTTP/1.1
Host: localhost:8080
Authorization: youshallnotpass
```

Response:
```json
{
  "loadType": "TRACK_LOADED",
  "playlistInfo": {},
  "tracks": [
    {
      "track": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
      "info": {
        "identifier": "dQw4w9WgXcQ",
        "isSeekable": true,
        "author": "RickAstleyVEVO",
        "length": 212000,
        "isStream": false,
        "position": 0,
        "title": "Rick Astley - Never Gonna Give You Up",
        "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
      }
    }
  ]
}
```

If the identifier leads to a playlist, `playlistInfo` will contain two properties, `name` and `selectedTrack`
```json
{
  "loadType": "PLAYLIST_LOADED",
  "playlistInfo": {
    "name": "Example YouTube Playlist",
    "selectedTrack": 3
  },
  "tracks": [
    ...
  ]
}
```

Additionally, in every `/loadtracks` response, a `loadType` property is returned which can be used to judge the response from Lavalink properly. It can be one of the following:
* `TRACK_LOADED` - Returned when a single track is loaded.
* `PLAYLIST_LOADED` - Returned when a playlist is loaded.
* `SEARCH_RESULT` - Returned when a search result is made (i.e `ytsearch: some song`).
* `NO_MATCHES` - Returned if no matches/sources could be found for a given identifier.
* `LOAD_FAILED` - Returned if Lavaplayer failed to load something for some reason.

### Special notes
* When your shard's mainWS connection dies, so does all your lavalink audio connections.
    * This also includes resumes
* When a client connection to Lavalink-Server disconnects, all connections and players for that session are shut down.
* If Lavalink-Server suddenly dies (think SIGKILL) the client will have to terminate any audio connections by sending this event:
```json
{"op":4,"d":{"self_deaf":false,"guild_id":"GUILD_ID_HERE","channel_id":null,"self_mute":false}}
```

# Common pitfalls
Admidtedly Lavalink isn't inherently the most intuitive thing ever, and people tend to run into the same mistakes over again. Please double check the following if you run into problems developing your client and you can't connect to a voice channel or play audio:

1. Check that you are forwarding sendWS events to **Discord**.
2. Check that you are intercepting **VOICE_SERVER_UPDATE**s to **Lavalink**. Do not edit the event object from Discord.
3. Check that you aren't expecting to hear audio when you have forgotten to queue something up OR forgotten to join a voice channel.
4. Check that you are not trying to create a voice connection with your Discord library.
5. When in doubt, check the debug logfile at `/logs/debug.log`.
