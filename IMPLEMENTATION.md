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
When opening a websocket connection, you must supply 2 required headers:
```
Authorization: Password matching the server config
Num-Shards: Total number of shards your bot is operating on
```

### Outgoing messages
Make the server queue a voice connection
```json
{
    "op": "connect",
    "guildId": "...",
    "channelId": "..."
}
```

Provide an intercepted voice server update
```json
{
    "op": "voiceUpdate",
    "sessionId": "...",
    "event": "..."
}
```

Close a voice connection
```json
{
    "op": "disconnect",
    "guildId": "123"
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

Cause the player to play a track
```json
{
    "op": "play",
    "guildId": "...",
    "track": "..."
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

Set player volume. Volume may range from 0 to 150. 100 is default.
```json
{
    "op": "volume",
    "guildId": "...",
    "volume": 125
}
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

Request to check if the VC or Guild exists, and that we have access to the VC
```json
{
    "op": "validationReq",
    "guildOrChannelId": "..."
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
  "tracks": [
    "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
  ]
}
```
