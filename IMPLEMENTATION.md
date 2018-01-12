
# Implementation guidelines
How to write your own client. The Java client will serve as an example implementation.

## Requirements
* You must be able to send messages via a shard's mainWS connection.
* You must be able to intercept voice server updates from mainWS on your shard connection.
* One of the following WS drafts (all but RFC 6455 is deprecated but should work):
    * RFC 6455
    * Hybi 17
    * Hybi 10
    * Hixie 76
    * Hixie 75

## Protocol
### Opening a connection
When opening a websocket connection, you must supply 3 required headers:
```
Authorization: Password matching the server config
Num-Shards: Total number of shards your bot is operating on
User-Id: The user id of the bot you are playing music with
```

### Hello op
After connecting, you will get sent back a hello op which contains the following:
```json
{
  "op" : "hello",
  "restBase": "http://localhost:2333/",
  "identifier": "..."
}
```

### Outgoing websocket messages
Provide an intercepted voice server update
```json
{
    "op": "voiceUpdate",
    "guildId": "...",
    "sessionId": "...",
    "event": "..."
}
```

Response to `validationReq`. `channelId` is omitted if the request does not display the channel id.
```json
{
    "op": "validationRes",
    "guildId": "...",
    "channelId": "...",
    "valid": true
}
```

Response to `isConnectedRes`.
```json
{
    "op": "isConnectedRes",
    "shardId": 1337,
    "connected": true
}
```

### Rest operations
#### All rest operations should include the `Authorization` header with the identifier provided via websocket in the hello op

**Play a track** `/{guildId}/play`:

Query params:
- `startTime`
- `endTime`

The track base64 string should be sent in the body of the request

`startTime` is an optional setting that determines the number of milliseconds to offset the track by. Defaults to 0.
`endTime` is an optional setting that determines at the number of milliseconds at which point the track should stop playing. Helpful if you only want to play a snippet of a bigger track. By default the track plays until it's end as per the encoded data.


**Stop the player** `/{guildId}/stop`

**Set player pause state** `/{guildId}/pause`

Query params:
- `pause` - boolean, sets the players paused state (true is paused)

**Seek** `/{guildId}/seek`

Query params:
- `position` - The desired position (in milliseconds)

**Volume** `/{guildId}/volume`

Query params:
- `volume` - Sets the desired volume, from 0 to 150, 100 is default.

**Resolving tracks for non JVM clients:**
```
GET /loadtracks?identifier=dQw4w9WgXcQ HTTP/1.1
Host: localhost:8080
Authorization: youshallnotpass
```

Response:
```json
[
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
```

### Incoming messages
See
[LavalinkSocket.java](https://github.com/Frederikam/Lavalink/blob/91bc0ef4dab6ca5d5efcba12203ee4054bb55ae9/LavalinkClient/src/main/java/lavalink/client/io/LavalinkSocket.java)
for client implementation

Incoming message to forward to mainWS
```json
{
    "op": "sendWS",
    "shardId": 1337,
    "message": "..."
}
```

Request to check if the VC or Guild exists, and that we have access to the VC. Note that the channelId may be omitted, in which case you should only check if we are in the guild.
```json
{
    "op": "validationReq",
    "guildId": "...",
    "channelId": "..."
}
```

Request to check if a shard's mainWS is connected
```json
{
    "op": "isConnectedReq",
    "shardId": 1337
}
```

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
