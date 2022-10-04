# Implementation guidelines
How to write your own client. The Java [Lavalink-Client](https://github.com/freyacodes/lavalink-client) will serve as an example implementation.
The Java client has support for JDA, but can also be adapted to work with other JVM libraries.

## Requirements
* You must be able to send messages via a shard's mainWS connection.
* You must be able to intercept voice server updates from mainWS on your shard connection.

## Significant changes v3.5 -> v3.6
* Deprecation of all endpoints and moved them to `/v3` namespace.
* Deprecation of all client messages (play, stop, pause, seek, volume, filters, destroy, voiceUpdate & configureResuming).
* Deprecation of [`/loadtracks` response](#track-loading).
* Deprecation of [track decoding response](#track-decoding).
* Addition of new WebSocket dispatch [Ready OP](#ready) to get `sessionId` and `resume` status.
* Addition of new [Session](#update-session)/[Player](#get-player) REST API.

## Significant changes v3.3 -> v3.4
* Added filters
* The `error` string on the `TrackExceptionEvent` has been deprecated and replaced by 
the `exception` object following the same structure as the `LOAD_FAILED` error on [`/loadtracks`](#track-loading)
* Added the `connected` boolean to player updates.
* Added source name to REST api track objects
* Clients are now requested to make their name known during handshake

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
User-Id: The user id of the bot you are playing music with
Client-Name: The name of your client. Optionally in the format NAME/VERSION
```

### Incoming messages

See [LavalinkSocket.java](https://github.com/freyacodes/lavalink-client/blob/master/src/main/java/lavalink/client/io/LavalinkSocket.java) for client implementation

Fields marked with `?` are optional.
Types marked with `?` are nullable.

#### Ready
Dispatched by Lavalink upon successful connection and authorization. Contains fields determining if resuming was successful, as well as the session ID.

| Field     | Type   | Description                                                                         |
|-----------|--------|-------------------------------------------------------------------------------------|
| op        | string | The type of payload                                                                 |
| resumed?  | bool   | If the session is resumed(Only present if a session id was present when connecting) |
| sessionId | string | The Lavalink Session ID of this connection                                          |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "ready",
    "resumed": false,
    "sessionId": "..."
}
```
</details>

#### Player Update
Dispatched every x(configurable in `application.yml`) seconds with the current state of the player.

| Field   | Type   | Description                              |
|---------|--------|------------------------------------------|
| op      | string | The type of payload                      |
| guildId | string | The guild id of the player               |
| state   | object | The [state](#player-state) of the player |

##### Player State
| Field     | Type   | Description                                                                           |
|-----------|--------|---------------------------------------------------------------------------------------|
| time      | string | Unix timestamp in milliseconds                                                        |
| position? | int    | The position of the track in milliseconds                                             |
| connected | bool   | If Lavalink is connected to the voice gateway                                         |
| ping      | int    | The ping of the node to the discord voice server in milliseconds(-1 if not connected) |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "playerUpdate",
    "guildId": "...",
    "state": {
        "time": 1500467109,
        "position": 60000,
        "connected": true,
        "ping": 0
    }
}
```
</details>

#### Stats
A collection of stats sent every minute. 

| Field          | Type   | Description                                 |
|----------------|--------|---------------------------------------------|
| op             | string | The type of payload                         |
| players        | int    | The amount of players connected to the node |
| playingPlayers | int    | The amount of players playing a track       |
| uptime         | int    | The uptime of the node in milliseconds      |
| memory         | object | The [memory](#memory) stats of the node     |
| cpu            | object | The [cpu](#cpu) stats of the node           |
| frameStats     | object | The [frame stats](#frame-stats) of the node |

##### Memory
| Field      | Type | Description                              |
|------------|------|------------------------------------------|
| free       | int  | The amount of free memory in bytes       |
| used       | int  | The amount of used memory in bytes       |
| allocated  | int  | The amount of allocated memory in bytes  |
| reservable | int  | The amount of reservable memory in bytes |

##### CPU
| Field        | Type   | Description                      |
|--------------|--------|----------------------------------|
| cores        | int    | The amount of cores the node has |
| systemLoad   | double | The system load of the node      |
| lavalinkLoad | double | The load of Lavalink on the node |

##### Frame Stats
| Field   | Type | Description                            |
|---------|------|----------------------------------------|
| sent    | int  | The amount of frames sent to Discord   |
| nulled  | int  | The amount of frames that were nulled  |
| deficit | int  | The amount of frames that were deficit |


<details open>
<summary>Example Payload</summary>

```json
{
    "op": "stats",
    "players": 1,
    "playingPlayers": 1,
    "uptime": 123456789,
    "memory": {
        "free": 123456789,
        "used": 123456789,
        "allocated": 123456789,
        "reservable": 123456789
    },
    "cpu": {
        "cores": 4,
        "systemLoad": 0.5,
        "lavalinkLoad": 0.5
    },
    "frameStats": {
        "sent": 123456789,
        "nulled": 123456789,
        "deficit": 123456789
    }
}
```
</details>


#### Event
Server emitted an event. See the client implementation below.

| Field | Type   | Description         |
|-------|--------|---------------------|
| op    | string | The type of payload |
| type  | string | The type of event   |
| guild | string | The guild id        |

<details open>
<summary>Example Payload</summary>

```yaml
{
    "op": "event",
    "type": "...",
    "guildId": "...",
    ...
}
```
</details>

| Event Type                                    | Description                                                              |
|-----------------------------------------------|--------------------------------------------------------------------------|
| [TrackStartEvent](#trackstartevent)           | Emitted when a track starts playing                                      |
| [TrackEndEvent](#TrackEndEvent)               | Emitted when a track ends                                                |
| [TrackExceptionEvent](#TrackExceptionEvent)   | Emitted when a track throws an exception                                 |
| [TrackStuckEvent](#TrackStuckEvent)           | Emitted when a track gets stuck while playing                            |
| [WebSocketClosedEvent](#WebSocketClosedEvent) | Emitted when the websocket connection to discord voice servers is closed |

##### TrackStartEvent
Emitted when a track starts playing.

| Field   | Type   | Description                              |
|---------|--------|------------------------------------------|
| track   | string | The track that started playing           |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "event",
    "type": "TrackStartEvent",
    "guildId": "...",
    "track": "..."
}
```

</details>

##### TrackEndEvent
Emitted when a track ends.

| Field  | Type           | Description                  |
|--------|----------------|------------------------------|
| track  | string         | The track that ended playing |
| reason | TrackEndReason | The reason the track ended   |

##### TrackEndReason
| Reason      | Description                | May Start Next |
|-------------|----------------------------|----------------|
| FINISH      | The track finished playing | true           |
| LOAD_FAILED | The track failed to load   | true           |
| STOPPED     | The track was stopped      | false          |
| REPLACED    | The track was replaced     | false          |
| CLEANUP     | The track was cleaned up   | false          |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "event",
    "type": "TrackEndEvent",
    "guildId": "...",
    "track": "...",
    "reason": "FINISHED"
}
```
<details>

##### TrackExceptionEvent
Emitted when a track throws an exception.

| Field     | Type   | Description                        |
|-----------|--------|------------------------------------|
| track     | string | The track that threw the exception |
| exception | object | The [Exception](#exception)        |

##### Exception
| Field    | Type     | Description                   |
|----------|----------|-------------------------------|
| message  | string   | The message of the exception  |
| severity | Severity | The severity of the exception |
| cause    | string   | The cause of the exception    |

##### Severity
| Severity   | Description |
|------------|-------------|
| COMMON     | Common      |
| SUSPICIOUS | Suspicious  |
| FATAL      | Fatal       |


<details open>
<summary>Example Payload</summary>

```json
{
    "op": "event",
    "type": "TrackExceptionEvent",
    "guildId": "...",
    "track": "...",
    "exception": {
        "message": "...",
        "severity": "COMMON",
        "cause": "..."
    }
}
```
</details>


##### TrackStuckEvent
Emitted when a track gets stuck while playing.

| Field       | Type   | Description                                     |
|-------------|--------|-------------------------------------------------|
| track       | string | The track that got stuck                        |
| thresholdMs | int    | The threshold in milliseconds that was exceeded |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "event",
    "type": "TrackStuckEvent",
    "guildId": "...",
    "track": "...",
    "thresholdMs": 123456789
}
```
</details>


##### WebSocketClosedEvent
Emitted when an audio web socket (to Discord) is closed.
This can happen for various reasons (normal and abnormal), e.g when using an expired voice server update.
4xxx codes are usually bad.
See the [Discord docs](https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes).

| Field    | Type   | Description                                                                                                                       |
|----------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| code     | int    | The [discord close event code](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes) |
| reason   | string | The close reason                                                                                                                  |
| byRemote | bool   | The close reason                                                                                                                  |

<details open>
<summary>Example Payload</summary>

```json
{
    "op": "event",
    "type": "WebSocketClosedEvent",
    "guildId": "...",
    "code": 4006,
    "reason": "Your session is no longer valid.",
    "byRemote": true
}
```
</details>


### Rest API
Lavalink exposes a REST API to allow for easy control of the players.
All routes require the Authorization header with the configured password.
```
Authorization: youshallnotpass
```

#### Get Players
Returns all players in this specific session.
```
GET /v3/sessions/{sessionId}/players
```

Response:
```json
[
  {
    "guildId": "...",
    "track": {
      "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000, // in ms
      "isStream": false,
      "position": 0, // in ms
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "sourceName": "youtube"
    },
    "volume": 100,
    "paused": false,
    "voice": {
      "token": "...",
      "endpoint": "...",
      "sessionId": "...",
      "connected": true,
      "ping": 10 // in ms & -1 if not connected
    },
    "filters": { ... }
  }
]
```

#### Get Player
Returns the player for this guild in this session.
```
GET /v3/sessions/{sessionId}/players/{guildId}
```

Response:
```json
{
  "guildId": "...",
  "track": {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000, // in ms
    "isStream": false,
    "position": 0, // in ms
    "title": "Rick Astley - Never Gonna Give You Up",
    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "sourceName": "youtube"
  },
  "volume": 100,
  "paused": false,
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "...",
    "connected": true,
    "ping": 10 // in ms & -1 if not connected
  },
  "filters": { ... }
}
```

#### Update Player
Updates the player for this guild in this specific.
```
PATCH /v3/sessions/{sessionId}/players/{guildId}?noReplace=true
```

All fields are optional.
You can provide either `trackData` or `identifier` or nothing, not both.
Request:
```json
{
    "trackData": "...",
    "identifier": "...",
    "startTime": 0, // in ms
    "endTime": 0, // in ms
    "volume": 100,
    "position": 32400, // in ms
    "pause": false,
    "filters": { ... },
    "sessionId": "...",
    "event": {
        "token": "...",
        "endpoint": "..."
    }
}
```

Response: 
```json
{
  "guildId": "...",
  "track": {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000, // in ms
    "isStream": false,
    "position": 0, // in ms
    "title": "Rick Astley - Never Gonna Give You Up",
    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "sourceName": "youtube"
  },
  "volume": 100,
  "paused": false,
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "...",
    "connected": true,
    "ping": 10 // in ms & -1 if not connected
  },
  "filters": { ... }
}
```

#### Filters
Filters are used in above requests and look like this
```js
{
  // Float value where 1.0 is 100%. Values >1.0 may cause clipping
  "volume": 1.0, // 0 ≤ x ≤ 5
  
  // There are 15 bands (0-14) that can be changed.
  // "gain" is the multiplier for the given band. The default value is 0. Valid values range from -0.25 to 1.0,
  // where -0.25 means the given band is completely muted, and 0.25 means it is doubled. Modifying the gain could
  // also change the volume of the output.
  "equalizer": [
    {
      "band": 0,  // 0 ≤ x ≤ 14
      "gain": 0.2 // -0.25 ≤ x ≤ 1
    }
  ],
  
  // Uses equalization to eliminate part of a band, usually targeting vocals.
  "karaoke": {
    "level": 1.0,
    "monoLevel": 1.0,
    "filterBand": 220.0,
    "filterWidth": 100.0
  },
  
  // Changes the speed, pitch, and rate. All default to 1.
  "timescale": {
    "speed": 1.0, // 0 ≤ x
    "pitch": 1.0, // 0 ≤ x
    "rate": 1.0   // 0 ≤ x
  },
  
  // Uses amplification to create a shuddering effect, where the volume quickly oscillates.
  // Example: https://en.wikipedia.org/wiki/File:Fuse_Electronics_Tremolo_MK-III_Quick_Demo.ogv
  "tremolo": {
    "frequency": 2.0, // 0 < x
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Similar to tremolo. While tremolo oscillates the volume, vibrato oscillates the pitch.
  "vibrato": {
    "frequency": 2.0, // 0 < x ≤ 14
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Rotates the sound around the stereo channels/user headphones aka Audio Panning. It can produce an effect similar to: https://youtu.be/QB9EB8mTKcc (without the reverb)
  "rotation": {
    "rotationHz": 0 // The frequency of the audio rotating around the listener in Hz. 0.2 is similar to the example video above.
  },
  
  // Distortion effect. It can generate some pretty unique audio effects.
  "distortion": {
    "sinOffset": 0.0,
    "sinScale": 1.0,
    "cosOffset": 0.0,
    "cosScale": 1.0,
    "tanOffset": 0.0,
    "tanScale": 1.0,
    "offset": 0.0,
    "scale": 1.0
  } 
  
  // Mixes both channels (left and right), with a configurable factor on how much each channel affects the other.
  // With the defaults, both channels are kept independent from each other.
  // Setting all factors to 0.5 means both channels get the same audio.
  "channelMix": {
    "leftToLeft": 1.0,
    "leftToRight": 0.0,
    "rightToLeft": 0.0,
    "rightToRight": 1.0,
  }
  
  // Higher frequencies get suppressed, while lower frequencies pass through this filter, thus the name low pass.
  // Any smoothing values equal to, or less than 1.0 will disable the filter.
  "lowPass": {
    "smoothing": 20.0 // 1.0 < x
  }
}
```

#### Destroy Player
Destroys the player for this guild in this session.
```
DELETE /v3/sessions/{sessionId}/players/{guildId}
```

Response: 

204 - No Content

#### Update Session
Updates the session with a resuming key and timeout.
```
PATCH /v3/sessions/{sessionId}
```

Request:
```json
{
    "resumingKey": "...",
    "timeout": 0 // in seconds
}
```

Response:

204 - No Content

#### Track Loading
This endpoint is used to resolve audio tracks for use with the [Update Player](#update-player) endpoint.
> `/loadtracks?identifier=dQw4w9WgXcQ` is deprecated and for removal in v4
```
GET /v3/loadtracks?identifier=dQw4w9WgXcQ
```
<details open>
<summary>Response(Deprecated)</summary>

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
        "length": 212000, // in ms
        "isStream": false,
        "position": 0, // in ms
        "title": "Rick Astley - Never Gonna Give You Up",
        "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "sourceName": "youtube"
      }
    }
  ]
}
```

</details>

Response:
```json
{
  "loadType": "TRACK_LOADED",
  "playlistInfo": {},
  "tracks": [
    {
      "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000, // in ms
      "isStream": false,
      "position": 0, // in ms
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "sourceName": "youtube"
    }
  ]
}
```

If the identifier leads to a playlist, `playlistInfo` will contain two properties, `name` and `selectedTrack`(-1 if no selectedTrack found)
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

If the loadType is `LOAD_FAILED`, the response will contain an `exception` object with `message` and `severity` properties.
`message` is a string detailing why the track failed to load, and is okay to display to end-users. Severity represents how common the error is.
A severity level of `COMMON` indicates that the error is non-fatal and that the issue is not from Lavalink itself.

```json
{
  "loadType": "LOAD_FAILED",
  "playlistInfo": {},
  "tracks": [],
  "exception": {
    "message": "The uploader has not made this video available in your country.",
    "severity": "COMMON"
  }
}
```

#### Track Searching
Lavalink supports searching via YouTube, YouTube Music, and Soundcloud. To search, you must prefix your identifier with `ytsearch:`, `ytmsearch:`,  or `scsearch:`respectively.

When a search prefix is used, the returned `loadType` will be `SEARCH_RESULT`. Note that, disabling the respective source managers renders these search prefixes redundant. Plugins may also implement prefixes to allow for more search engines.

#### Track Decoding

Decode a single track into its info
> `/decodetrack?track=QAAAjJ...AAA==` is deprecated and for removal in v4
```
GET /v3/decodetrack?track=QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==
```

Response:
```json
{
  "identifier": "dQw4w9WgXcQ",
  "isSeekable": true,
  "author": "RickAstleyVEVO",
  "length": 212000, // in ms
  "isStream": false,
  "position": 0, // in ms
  "title": "Rick Astley - Never Gonna Give You Up",
  "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "sourceName": "youtube"
}
```

Decodes multiple tracks into their info
> `/decodetracks` is deprecated and for removal in v4
```
POST /v3/decodetracks
Authorization: youshallnotpass
```

Request:
```json
[
   "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
   ...
]
```
<details open>
<summary>Response(Deprecated):</summary>

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
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "sourceName": "youtube"
    }
  },
  ...
]
```
</details>

Response:
```json
[
  {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000, // in ms
    "isStream": false,
    "position": 0, // in ms
    "title": "Rick Astley - Never Gonna Give You Up",
    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "sourceName": "youtube"
  },
  ...
]
```
---

#### Get Plugins
Request information about the plugins running on Lavalink, if any.
> `/plugins` is deprecated and for removal in v4
```
GET /v3/plugins
```

Response:
```yaml
[
  {
    "name": "some-plugin",
    "version": "1.0.0"
  },
  {
    "name": "foo-plugin",
    "version": "1.2.3"
  }
]
```

#### Get Lavalink info
Request Lavalink information.
```
GET /v3/info
Authorization: youshallnotpass
```

Response:
```json
{
  "version": {
    "version": "v3.6.0",
    "major": 3,
    "minor": 6,
    "patch": 0
  },
  "buildTime": 1664223916812,
  "git": {
    "branch": "master",
    "commit": "85c5ab5",
    "commitTime": 1664223916812
  },
  "jvm": "18.0.2.1",
  "lavaplayer": "1.3.98.4-original",
  "sourceManagers": [
    "youtube",
    "soundcloud"
  ]
}
```

#### Get Lavalink version(DEPRECATED)
Request Lavalink version.
> `/version` is deprecated and for removal in v4 use [`/info`](#get-lavalink-info) instead
```
GET /version
Authorization: youshallnotpass
```

Response:
```
v3.5
```


### RoutePlanner API

Additionally, there are a few REST endpoints for the ip rotation extension

#### Get RoutePlanner status
> `/routeplanner/status` is deprecated and for removal in v4
```
GET /v3/routeplanner/status
```

Response:

```json
{
  "class": "RotatingNanoIpRoutePlanner",
  "details": {
    "ipBlock": {
      "type": "Inet6Address",
      "size": "1208925819614629174706176"
    },
    "failingAddresses": [
      {
        "address": "/1.0.0.0",
        "failingTimestamp": 1573520707545,
        "failingTime": "Mon Nov 11 20:05:07 EST 2019"
      }
    ],
    "blockIndex": "0",
    "currentAddressIndex": "36792023813"
  }
}
```

The response is different based on each route planner. 
Fields which are always present are: `class`, `details.ipBlock` and 
`details.failingAddresses`. If no route planner is set, both `class` and
`details` will be null, and the other endpoints will result in status 500.

The following classes have additional detail fields:

#### RotatingIpRoutePlanner

`details.rotateIndex` String containing the number of rotations which happened 
since the restart of Lavalink

`details.ipIndex` String containing the current offset in the block

`details.currentAddress` The currently used ip address

#### NanoIpRoutePlanner

`details.currentAddressIndex` long representing the current offset in the ip 
block

#### RotatingNanoIpRoutePlanner

`details.blockIndex` String containing the the information in which /64 block ips 
are chosen. This number increases on each ban.

`details.currentAddressIndex` long representing the current offset in the ip 
block.

#### Unmark a failed address
> `/routeplanner/free/address` is deprecated and for removal in v4
```
POST /v3/routeplanner/free/address
```

Request Body:

```json
{
  "address": "1.0.0.1"
}
```

Response:

204 - No Content

#### Unmark all failed address
> `/routeplanner/free/all` is deprecated and for removal in v4
```
POST /v3/routeplanner/free/all
```

Response:

204 - No Content

---

All REST responses from Lavalink include a `Lavalink-Api-Version` header.

### Resuming Lavalink sessions

What happens after your client disconnects is dependent on whether or not the session has been configured for resuming.

* If resuming is disabled all voice connections are closed immediately.
* If resuming is enabled all music will continue playing. You will then be able to resume your session, allowing you to control the players again.

To enable resuming, you must send a `configureResuming` message.

* `key` is the string you will need to send when resuming the session. Set to null to disable resuming altogether. Defaults to null.
* `timeout` is the number of seconds after disconnecting before the session is closed anyways. This is useful for avoiding accidental leaks. Defaults to `60` (seconds).

```json
{
  "op": "configureResuming",
  "key": "myResumeKey",
  "timeout": 60
}
```

To resume a session, specify the resume key in your WebSocket handshake request headers:

```
Resume-Key: The resume key of the session you want to resume.
```

You can tell if your session was resumed by looking at the handshake response header `Session-Resumed` which is either `true` or `false`:

```
Session-Resumed: true
```

When a session is paused, any events that would normally have been sent is queued up. When the session is resumed, this
queue is then emptied and the events are then replayed. 

### Special notes

* When your shard's main WS connection dies, so does all your lavalink audio connections.
  * This also includes resumes

* If Lavalink-Server suddenly dies (think SIGKILL) the client will have to terminate any audio connections by sending this event:

```json
{"op":4,"d":{"self_deaf":false,"guild_id":"GUILD_ID_HERE","channel_id":null,"self_mute":false}}
```

### Outgoing messages(DEPRECATED)


#### Provide a voice server update(DEPRECATED)

Provide an intercepted voice server update. This causes the server to connect to the voice channel.

```js
{
  "op": "voiceUpdate",
  "guildId": "...",
  "sessionId": "...",
  "event": {...}
}
```

#### Play a track(DEPRECATED)

`startTime` is an optional setting that determines the number of milliseconds to offset the track by. Defaults to 0.

`endTime` is an optional setting that determines at the number of milliseconds at which point the track should stop playing. Helpful if you only want to play a snippet of a bigger track. By default the track plays until it's end as per the encoded data.

`volume` is an optional setting which changes the volume if provided.

If `noReplace` is set to true, this operation will be ignored if a track is already playing or paused. This is an optional field.

If `pause` is set to true, the playback will be paused. This is an optional field.

```json
{
  "op": "play",
  "guildId": "...",
  "track": "...",
  "startTime": "60000",
  "endTime": "120000",
  "volume": "100",
  "noReplace": false,
  "pause": false
}
```

#### Stop a player(DEPRECATED)

```json
{
  "op": "stop",
  "guildId": "..."
}
```

#### Pause the playback(DEPRECATED)

```json
{
  "op": "pause",
  "guildId": "...",
  "pause": true
}
```

#### Seek a track(DEPRECATED)

The position is in milliseconds.

```json
{
  "op": "seek",
  "guildId": "...",
  "position": 60000
}
```

#### Set player volume(DEPRECATED)

Volume may range from 0 to 1000. 100 is default.

```json
{
  "op": "volume",
  "guildId": "...",
  "volume": 125
}
```

#### Using filters(DEPRECATED)

The `filters` op sets the filters. All the filters are optional, and leaving them out of this message will disable them.

Adding a filter can have adverse effects on performance. These filters force Lavaplayer to decode all audio to PCM,
even if the input was already in the Opus format that Discord uses. This means decoding and encoding audio that would
normally require very little processing. This is often the case with YouTube videos.

JSON comments are for illustration purposes only, and will not be accepted by the server.

Note that filters may take a moment to apply.

```yaml
{
  "op": "filters",
  "guildId": "...",
  
  // Float value where 1.0 is 100%. Values >1.0 may cause clipping
  "volume": 1.0, // 0 ≤ x ≤ 5
  
  // There are 15 bands (0-14) that can be changed.
  // "gain" is the multiplier for the given band. The default value is 0. Valid values range from -0.25 to 1.0,
  // where -0.25 means the given band is completely muted, and 0.25 means it is doubled. Modifying the gain could
  // also change the volume of the output.
  "equalizer": [
    {
      "band": 0,  // 0 ≤ x ≤ 14
      "gain": 0.2 // -0.25 ≤ x ≤ 1
    }
  ],
  
  // Uses equalization to eliminate part of a band, usually targeting vocals.
  "karaoke": {
    "level": 1.0,
    "monoLevel": 1.0,
    "filterBand": 220.0,
    "filterWidth": 100.0
  },
  
  // Changes the speed, pitch, and rate. All default to 1.
  "timescale": {
    "speed": 1.0, // 0 ≤ x
    "pitch": 1.0, // 0 ≤ x
    "rate": 1.0   // 0 ≤ x
  },
  
  // Uses amplification to create a shuddering effect, where the volume quickly oscillates.
  // Example: https://en.wikipedia.org/wiki/File:Fuse_Electronics_Tremolo_MK-III_Quick_Demo.ogv
  "tremolo": {
    "frequency": 2.0, // 0 < x
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Similar to tremolo. While tremolo oscillates the volume, vibrato oscillates the pitch.
  "vibrato": {
    "frequency": 2.0, // 0 < x ≤ 14
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Rotates the sound around the stereo channels/user headphones aka Audio Panning. It can produce an effect similar to: https://youtu.be/QB9EB8mTKcc (without the reverb)
  "rotation": {
    "rotationHz": 0 // The frequency of the audio rotating around the listener in Hz. 0.2 is similar to the example video above.
  },
  
  // Distortion effect. It can generate some pretty unique audio effects.
  "distortion": {
    "sinOffset": 0.0,
    "sinScale": 1.0,
    "cosOffset": 0.0,
    "cosScale": 1.0,
    "tanOffset": 0.0,
    "tanScale": 1.0,
    "offset": 0.0,
    "scale": 1.0
  } 
  
  // Mixes both channels (left and right), with a configurable factor on how much each channel affects the other.
  // With the defaults, both channels are kept independent from each other.
  // Setting all factors to 0.5 means both channels get the same audio.
  "channelMix": {
    "leftToLeft": 1.0,
    "leftToRight": 0.0,
    "rightToLeft": 0.0,
    "rightToRight": 1.0,
  }
  
  // Higher frequencies get suppressed, while lower frequencies pass through this filter, thus the name low pass.
  // Any smoothing values equal to, or less than 1.0 will disable the filter.
  "lowPass": {
    "smoothing": 20.0 // 1.0 < x
  }
}
```

#### Destroy a player(DEPRECATED)

Tell the server to potentially disconnect from the voice server and potentially remove the player with all its data.
This is useful if you want to move to a new node for a voice connection. Calling this op does not affect voice state,
and you can send the same VOICE_SERVER_UPDATE to a new node.

```json
{
  "op": "destroy",
  "guildId": "..."
}
```

# Common pitfalls
Admittedly Lavalink isn't inherently the most intuitive thing ever, and people tend to run into the same mistakes over again. Please double check the following if you run into problems developing your client and you can't connect to a voice channel or play audio:

1. Check that you are forwarding sendWS events to **Discord**.
2. Check that you are intercepting **VOICE_SERVER_UPDATE**s to **Lavalink**. Do not edit the event object from Discord.
3. Check that you aren't expecting to hear audio when you have forgotten to queue something up OR forgotten to join a voice channel.
4. Check that you are not trying to create a voice connection with your Discord library.
5. When in doubt, check the debug logfile at `/logs/debug.log`.
