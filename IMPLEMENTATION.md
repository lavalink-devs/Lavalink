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
* Addition of new WebSocket dispatch [Ready OP](#ready-op) to get `sessionId` and `resume` status.
* Addition of new [Session](#update-session)/[Player](#get-player) REST API.

## Significant changes v3.3 -> v3.4
* Added filters
* The `error` string on the `TrackExceptionEvent` has been deprecated and replaced by 
the [exception object](#exception-object)  following the same structure as the `LOAD_FAILED` error on [`/loadtracks`](#track-loading)
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

With Lavalink 1.x the server had the responsibility of handling Discord `VOICE_SERVER_UPDATE`s as well as its own internal ratelimiting.
This remote handling makes things unnecessarily complicated and adds a lot og points where things could go wrong. 
One problem we noticed is that since JD-AA is unaware of ratelimits on the bots gateway connection, it would keep adding
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

### Websocket Messages

See [LavalinkSocket.java](https://github.com/freyacodes/lavalink-client/blob/master/src/main/java/lavalink/client/io/LavalinkSocket.java) for client implementation

Fields marked with `?` are optional.
Types marked with `?` are nullable.

Websocket messages all follow the following standard format:

| Field | Type                 | Description                           |
|-------|----------------------|---------------------------------------|
| op    | [OP Type](#op-types) | The op type                           |
| ...   | ...                  | Extra fields depending on the op type |

<details>
<summary>Example Payload</summary>

```yaml
{
  "op": "...",
  ...
}
```
</details>

#### OP Types

| OP Type                           | Description                                                  |
|-----------------------------------|--------------------------------------------------------------|
| [ready](#ready-op)                | Emitted when you successfully connected to the Lavalink node |
| [playerUpdate](#player-update-op) | Emitted when every x seconds with the latest player state    |
| [event](#event-op)                | Emitted when an event is emitted                             |

#### Ready OP
Dispatched by Lavalink upon successful connection and authorization. Contains fields determining if resuming was successful, as well as the session ID.

| Field     | Type   | Description                                                                         |
|-----------|--------|-------------------------------------------------------------------------------------|
| resumed?  | bool   | If the session is resumed(Only present if a session id was present when connecting) |
| sessionId | string | The Lavalink Session ID of this connection                                          |

<details>
<summary>Example Payload</summary>

```json
{
  "op": "ready",
  "resumed": false,
  "sessionId": "..."
}
```
</details>

---

#### Player Update OP
Dispatched every x(configurable in `application.yml`) seconds with the current state of the player.

| Field   | Type                                 | Description                |
|---------|--------------------------------------|----------------------------|
| guildId | string                               | The guild id of the player |
| state   | [Player State](#player-state) object | The player state           |

##### Player State
| Field     | Type   | Description                                                                           |
|-----------|--------|---------------------------------------------------------------------------------------|
| time      | string | Unix timestamp in milliseconds                                                        |
| position? | int    | The position of the track in milliseconds                                             |
| connected | bool   | If Lavalink is connected to the voice gateway                                         |
| ping      | int    | The ping of the node to the discord voice server in milliseconds(-1 if not connected) |

<details>
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

---

#### Stats OP
A collection of stats sent every minute. 

##### Stats Object

| Field          | Type                               | Description                                 |
|----------------|------------------------------------|---------------------------------------------|
| players        | int                                | The amount of players connected to the node |
| playingPlayers | int                                | The amount of players playing a track       |
| uptime         | int                                | The uptime of the node in milliseconds      |
| memory         | [Memory](#memory) object           | The memory stats of the node                |
| cpu            | [CPU](#cpu) object                 | The cpu stats of the node                   |
| frameStats     | [Frame Stats](#frame-stats) object | The frame stats of the node                 |

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


<details>
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

---

#### Event OP
Server emitted an event. See the client implementation below.

| Field   | Type                      | Description                         |
|---------|---------------------------|-------------------------------------|
| type    | [EventType](#event-types) | The type of event                   |
| guildId | string                    | The guild id                        |
| ...     | ...                       | Extra fields depending on the event |

<details>
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

##### Event Types
| Event Type                                    | Description                                                              |
|-----------------------------------------------|--------------------------------------------------------------------------|
| [TrackStartEvent](#trackstartevent)           | Emitted when a track starts playing                                      |
| [TrackEndEvent](#trackendevent)               | Emitted when a track ends                                                |
| [TrackExceptionEvent](#trackexceptionevent)   | Emitted when a track throws an exception                                 |
| [TrackStuckEvent](#trackstuckevent)           | Emitted when a track gets stuck while playing                            |
| [WebSocketClosedEvent](#websocketclosedevent) | Emitted when the websocket connection to discord voice servers is closed |

##### TrackStartEvent
Emitted when a track starts playing.

| Field   | Type   | Description                              |
|---------|--------|------------------------------------------|
| track   | string | The track that started playing           |

<details>
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

---

##### TrackEndEvent
Emitted when a track ends.

| Field  | Type                                | Description                  |
|--------|-------------------------------------|------------------------------|
| track  | string                              | The track that ended playing |
| reason | [TrackEndReason](#track-end-reason) | The reason the track ended   |

##### Track End Reason
| Reason      | Description                | May Start Next |
|-------------|----------------------------|----------------|
| FINISH      | The track finished playing | true           |
| LOAD_FAILED | The track failed to load   | true           |
| STOPPED     | The track was stopped      | false          |
| REPLACED    | The track was replaced     | false          |
| CLEANUP     | The track was cleaned up   | false          |

<details>
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
</details>

---

##### TrackExceptionEvent
Emitted when a track throws an exception.

| Field     | Type                                  | Description                        |
|-----------|---------------------------------------|------------------------------------|
| track     | string                                | The track that threw the exception |
| exception | [Exception](#exception-object) object | The occurred exception             |

##### Exception Object
| Field    | Type                  | Description                   |
|----------|-----------------------|-------------------------------|
| message  | string                | The message of the exception  |
| severity | [Severity](#severity) | The severity of the exception |
| cause    | string                | The cause of the exception    |

##### Severity
| Severity   | Description                                                                                                                                                                                                                            |
|------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| COMMON     | The cause is known and expected, indicates that there is nothing wrong with the library itself                                                                                                                                         |
| SUSPICIOUS | The cause might not be exactly known, but is possibly caused by outside factors. For example when an outside service responds in a format that we do not expect                                                                        |
| FATAL      | If the probable cause is an issue with the library or when there is no way to tell what the cause might be. This is the default level and other levels are used in cases where the thrower has more in-depth knowledge about the error |


<details>
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

---

##### TrackStuckEvent
Emitted when a track gets stuck while playing.

| Field       | Type   | Description                                     |
|-------------|--------|-------------------------------------------------|
| track       | string | The track that got stuck                        |
| thresholdMs | int    | The threshold in milliseconds that was exceeded |

<details>
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

---

##### WebSocketClosedEvent
Emitted when an audio web socket (to Discord) is closed.
This can happen for various reasons (normal and abnormal), e.g. when using an expired voice server update.
4xxx codes are usually bad.
See the [Discord docs](https://discordapp.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes).

| Field    | Type   | Description                                                                                                                       |
|----------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| code     | int    | The [discord close event code](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes) |
| reason   | string | The close reason                                                                                                                  |
| byRemote | bool   | If the connection was closed by discord                                                                                           |

<details>
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

---

### Rest API
Lavalink exposes a REST API to allow for easy control of the players.
All routes require the Authorization header with the configured password.
```
Authorization: youshallnotpass
```

All routes are prefixed with `/v3` as of `v3.6` and the old are deprecated.

#### Get Players
Returns a list players in this specific session.
```
GET /v3/sessions/{sessionId}/players
```

##### Player
| Field   | Type                               | Description                    |
|---------|------------------------------------|--------------------------------|
| guildId | string                             | The guild id of the player     |
| track   | ?[Track](#track) object            | The current playing track      |
| volume  | int                                | The volume of the player       |
| paused  | bool                               | Whether the player is paused   |
| voice   | [Voice State](#voice-state) object | The voice state of the player  |
| filters | ?[Filters](#filters) object        | The filters used by the player |              

##### Track
| Field      | Type    | Description                        |
|------------|---------|------------------------------------|
| trackData  | string  | The encoded track data             |
| identifier | string  | The track identifier               |
| isSeekable | bool    | Whether the track is seekable      |
| author     | string  | The track author                   |
| length     | int     | The track length in milliseconds   |
| isStream   | bool    | Whether the track is a stream      |
| position   | int     | The track position in milliseconds |
| title      | string  | The track title                    |
| uri        | ?string | The track uri                      |
| sourceName | string  | The track source name              |

#### Voice State
| Field     | Type   | Description                     |
|-----------|--------|---------------------------------|
| token     | string | The voice token                 |
| endpoint  | string | The voice endpoint              |
| sessionId | string | The voice session id            |
| connected | bool   | Whether the player is connected |
| ping      | int    | The voice ping                  |

<details>
<summary>Example Payload</summary>

```yaml
[
  {
    "guildId": "...",
    "track": {
      "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000, 
      "isStream": false,
      "position": 0, 
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
      "ping": 10 
    },
    "filters": { ... }
  }
]
```
</details>

---

#### Get Player
Returns the player for this guild in this session.
```
GET /v3/sessions/{sessionId}/players/{guildId}
```

Response:

[Player Object](#Player)
<details>
<summary>Example Payload</summary>

```yaml
{
  "guildId": "...",
  "track": {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000,
    "isStream": false,
    "position": 0,
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
    "ping": 10
  },
  "filters": { ... }
}
```
</details>

---

#### Update Player
Updates the player for this guild in this specific.
```
PATCH /v3/sessions/{sessionId}/players/{guildId}?noReplace=true
```

You can provide either `trackData` or `identifier`, not both.

Request:

##### Player Update
| Field       | Type                               | Description                                                                                                                                   |
|-------------|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| trackData?  | ?string                            | The encoded track data to play. `null` stops the current track                                                                                |
| identifier? | string                             | The track identifier to play                                                                                                                  |
| position?   | int                                | The position in milliseconds                                                                                                                  |
| endTime?    | int                                | The end time in milliseconds                                                                                                                  |
| volume?     | int                                | The volume from 0 to 200                                                                                                                      |
| paused?     | bool                               | Whether the player is paused                                                                                                                  |
| filters?    | ?[Filters](#filters) object        | The new filters to apply. `null` will reset all filters                                                                                       |
| sessionId?  | string                             | The session id from the [voice state update event](https://discord.com/developers/docs/topics/gateway-events#voice-state-update) from discord |
| event?      | [Voice Event](#voice-event) object | The [voice server update event object](https://discord.com/developers/docs/topics/gateway-events#voice-server-update) from discord            |

##### Voice Event
| Field    | Type   | Description        |
|----------|--------|--------------------|
| token    | string | The voice token    |
| endpoint | string | The voice endpoint |

<details>
<summary>Example Payload</summary>

```yaml
{
  "trackData": "...",
  "identifier": "...",
  "startTime": 0, 
  "endTime": 0, 
  "volume": 100,
  "position": 32400,
  "paused": false,
  "filters": { ... },
  "sessionId": "...",
  "event": {
    "token": "...",
    "endpoint": "..."
  }
}
```
</details>


Response: 

[Player Object](#Player)

<details>
<summary>Example Payload</summary>

```yaml
{
  "guildId": "...",
  "track": {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000,
    "isStream": false,
    "position": 0,
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
    "ping": 10 
  },
  "filters": { ... }
}
```
</details>

---

#### Filters
Filters are used in above requests and look like this

| Field       | Type                               | Description                                                                                       |
|-------------|------------------------------------|---------------------------------------------------------------------------------------------------|
| volume?     | float                              | Lets you adjust the player volume from 0 to 5.0 where 1.0 is 100%. Values >1.0 may cause clipping |
| equalizer?  | array of [Equalizers](#equalizer)  | Lets you adjust 15 different bands                                                                |
| karaoke?    | [Karaoke](#karaoke)    object      | Lets you eliminate part of a band, usually targeting vocals.                                      |
| timescale?  | [Timescale](#timescale) object     | Lets you change the speed, pitch, and rate                                                        |
| tremolo?    | [Tremolo](#tremolo) object         | Lets you create a shuddering effect, where the volume quickly oscillates                          |
| vibrato?    | [Vibrato](#vibrato) object         | Lets you create a shuddering effect, where the pitch quickly oscillates                           |
| rotation?   | [Rotation](#rotation) object       | Lets you rotate the sound around the stereo channels/user headphones aka Audio Panning            |
| distortion? | [Distortion](#distortion) object   | Lets you distort the audio                                                                        |
| channelMix? | [Channel Mix](#channel-mix) object | Lets you mix both channels (left and right)                                                       |
| lowPass?    | [Low Pass](#low-pass) object       | Lets you filter higher frequencies                                                                |

##### Equalizer
There are 15 bands (0-14) that can be changed.
"gain" is the multiplier for the given band. The default value is 0. Valid values range from -0.25 to 1.0,
where -0.25 means the given band is completely muted, and 0.25 means it is doubled. Modifying the gain could also change the volume of the output.

| Field | Type  | Description             |
|-------|-------|-------------------------|
| bands | int   | The band (0 to 14)      |
| gain  | float | The gain (-0.25 to 1.0) |

##### Karaoke
Uses equalization to eliminate part of a band, usually targeting vocals.

| Field        | Type  | Description                                                             |
|--------------|-------|-------------------------------------------------------------------------|
| level?       | float | The level (0 to 1.0 where 0.0 is no effect and 1.0 is full effect)      |
| monoLevel?   | float | The mono level (0 to 1.0 where 0.0 is no effect and 1.0 is full effect) |
| filterBand?  | float | The filter band                                                         |
| filterWidth? | float | The filter width (0.1 to 1.0 where 0.1 is narrow and 1.0 is wide)       |

##### Timescale
Changes the speed, pitch, and rate. All default to 1.

| Field  | Type  | Description              |
|--------|-------|--------------------------|
| speed? | float | The playback speed 0 ≤ x |
| pitch? | float | The pitch 0 ≤ x          |
| rate?  | float | The rate 0 ≤ x           |

##### Tremolo
Uses amplification to create a shuddering effect, where the volume quickly oscillates.
https://en.wikipedia.org/wiki/File:Fuse_Electronics_Tremolo_MK-III_Quick_Demo.ogv

| Field      | Type  | Description                  |
|------------|-------|------------------------------|
| frequency? | float | The frequency 0 < x          |
| depth?     | float | The tremolo depth 0 < x ≤ 1  |

##### Vibrato
Similar to tremolo. While tremolo oscillates the volume, vibrato oscillates the pitch.

| Field      | Type  | Description                 |
|------------|-------|-----------------------------|
| frequency? | float | The frequency 0 < x ≤ 14    |
| depth?     | float | The vibrato depth 0 < x ≤ 1 |

##### Rotation
Rotates the sound around the stereo channels/user headphones aka Audio Panning. It can produce an effect similar to https://youtu.be/QB9EB8mTKcc (without the reverb)

| Field       | Type  | Description                                                                                              |
|-------------|-------|----------------------------------------------------------------------------------------------------------|
| rotationHz? | float | The frequency of the audio rotating around the listener in Hz. 0.2 is similar to the example video above |

##### Distortion
Distortion effect. It can generate some pretty unique audio effects.

| Field      | Type  | Description    |
|------------|-------|----------------|
| sinOffset? | float | The sin offset |
| sinScale?  | float | The sin scale  |
| cosOffset? | float | The cos offset |
| cosScale?  | float | The cos scale  |
| tanOffset? | float | The tan offset |
| tanScale?  | float | The tan scale  |
| offset?    | float | The offset     |
| scale?     | float | The scale      |

##### Channel Mix
Mixes both channels (left and right), with a configurable factor on how much each channel affects the other.
With the defaults, both channels are kept independent of each other.
Setting all factors to 0.5 means both channels get the same audio.

| Field         | Type  | Description                                           |
|---------------|-------|-------------------------------------------------------|
| leftToLeft?   | float | The left to left channel mix factor (0.0 ≤ x ≤ 1.0)   |
| leftToRight?  | float | The left to right channel mix factor (0.0 ≤ x ≤ 1.0)  |
| rightToLeft?  | float | The right to left channel mix factor (0.0 ≤ x ≤ 1.0)  |
| rightToRight? | float | The right to right channel mix factor (0.0 ≤ x ≤ 1.0) |

##### Low Pass
Higher frequencies get suppressed, while lower frequencies pass through this filter, thus the name low pass.
Any smoothing values equal to, or less than 1.0 will disable the filter.

| Field      | Type  | Description                    |
|------------|-------|--------------------------------|
| smoothing? | float | The smoothing factor (1.0 < x) |

<details>
<summary>Example Payload</summary>

```json
{
  "volume": 1.0,
  "equalizer": [
    {
      "band": 0,
      "gain": 0.2
    }
  ],
  "karaoke": {
    "level": 1.0,
    "monoLevel": 1.0,
    "filterBand": 220.0,
    "filterWidth": 100.0
  },
  "timescale": {
    "speed": 1.0,
    "pitch": 1.0,
    "rate": 1.0
  },
  "tremolo": {
    "frequency": 2.0,
    "depth": 0.5
  },
  "vibrato": {
    "frequency": 2.0,
    "depth": 0.5
  },
  "rotation": {
    "rotationHz": 0
  },
  "distortion": {
    "sinOffset": 0.0,
    "sinScale": 1.0,
    "cosOffset": 0.0,
    "cosScale": 1.0,
    "tanOffset": 0.0,
    "tanScale": 1.0,
    "offset": 0.0,
    "scale": 1.0
  },
  "channelMix": {
    "leftToLeft": 1.0,
    "leftToRight": 0.0,
    "rightToLeft": 0.0,
    "rightToRight": 1.0
  },
  "lowPass": {
    "smoothing": 20.0
  }
}
```
</details>

---

#### Destroy Player
Destroys the player for this guild in this session.
```
DELETE /v3/sessions/{sessionId}/players/{guildId}
```

Response: 

204 - No Content

---

#### Update Session
Updates the session with a resuming key and timeout.
```
PATCH /v3/sessions/{sessionId}
```

Request:

| Field       | Type   | Description                                              |
|-------------|--------|----------------------------------------------------------|
| resumingKey | string | The resuming key to be able to resume this session later |
| timeout     | int    | The timeout in seconds                                   |

<details>
<summary>Example Payload</summary>

```json
{
  "resumingKey": "...",
  "timeout": 0
}
```
</details>

Response:

204 - No Content

---

#### Track Loading
This endpoint is used to resolve audio tracks for use with the [Update Player](#update-player) endpoint.
> `/loadtracks?identifier=dQw4w9WgXcQ` is deprecated and marked for removal in v4
```
GET /v3/loadtracks?identifier=dQw4w9WgXcQ
```
<details>
<summary>Response(Deprecated)</summary>

```json
{
  "loadType": "TRACK_LOADED",
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
        "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "sourceName": "youtube"
      }
    }
  ]
}
```
</details>

Response:

##### Track Loading Result

| Field         | Type                                   | Description                                               | Valid Types                                        |
|---------------|----------------------------------------|-----------------------------------------------------------|----------------------------------------------------|
| loadType      | [LoadResultType](#load-result-type)    | The type of the result                                    | all                                                |
| playlistInfo? | [Playlist Info](#playlist-info) object | Additional info if the the load type is `PLAYLIST_LOADED` | `PLAYLIST_LOADED`                                  |
| tracks        | array of [Tracks](#track)              | All tracks which have been loaded                         | `TRACK_LOADED`, `PLAYLIST_LOADED`, `SEARCH_RESULT` |
| exception?    | [Exception](#exception-object) object  | The [Exception](#exception-object) this load failed with  | `LOAD_FAILED`                                      |

###### Load Result Type

| Load Result Type  | Description                                  |
|-------------------|----------------------------------------------|
| `TRACK_LOADED`    | A track has been loaded                      |
| `PLAYLIST_LOADED` | A playlist has been loaded                   |
| `SEARCH_RESULT`   | A search result has been loaded              |
| `NO_MATCHES`      | There has been no matches to your identifier |
| `LOAD_FAILED`     | Loading has failed                           |

###### Playlist Info

| Field         | Type   | Description                                                      |
|---------------|--------|------------------------------------------------------------------|
| name          | string | The name of the loaded playlist                                  |
| selectedTrack | int    | The selected track in this Playlist (-1 if no track is selected) |

<details>
<summary>Track Loaded Example Payload</summary>

```json
{
  "loadType": "TRACK_LOADED",
  "tracks": [
    {
      "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
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
  ]
}
```
</details>

<details>
<summary>Playlist Loaded Example Payload</summary>

```yaml
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
</details>

<details>
<summary>Search Result Example Payload</summary>

```yaml
{
  "loadType": "SEARCH_RESULT",
  "tracks": [
    ...
  ]
}
```
</details>

<details>
<summary>No Matches Example Payload</summary>

```json
{
  "loadType": "NO_MATCHES"
}
```
</details>

<details>
<summary>Load Failed Example Payload</summary>

```json
{
  "loadType": "LOAD_FAILED",
  "exception": {
    "message": "The uploader has not made this video available in your country.",
    "severity": "COMMON"
  }
}
```
</details>

#### Track Searching
Lavalink supports searching via YouTube, YouTube Music, and Soundcloud. To search, you must prefix your identifier with `ytsearch:`, `ytmsearch:`,  or `scsearch:`respectively.

When a search prefix is used, the returned `loadType` will be `SEARCH_RESULT`. Note that, disabling the respective source managers renders these search prefixes redundant. Plugins may also implement prefixes to allow for more search engines.

---

#### Track Decoding

Decode a single track into its info
> `/decodetrack?track=QAAAjJ...AAA==` is deprecated and marked for removal in v4
```
GET /v3/decodetrack?track=QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==
```

Response:

[Track Object](#track) without the `trackData` field

<details>
<summary>Example Payload</summary>

```json
{
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
```
</details>

<details>
<summary>Response(Deprecated)</summary>

```json
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
}
```
</details>

---

Decodes multiple tracks into their info
> `/decodetracks` is deprecated and marked for removal in v4
```
POST /v3/decodetracks
```

Request:

Array of track data strings

<details>
<summary>Example Payload</summary>

```yaml
[
   "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
   ...
]
```
</details>

Response:

Array of [Track Objects](#track)

<details>
<summary>Example Payload</summary>

```yaml
[
  {
    "trackData": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000,
    "isStream": false,
    "position": 0,
    "title": "Rick Astley - Never Gonna Give You Up",
    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "sourceName": "youtube"
  },
  ...
]
```
</details>

<details>
<summary>Response(Deprecated)</summary>

```yaml
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

---

#### Get Lavalink info
Request Lavalink information.
```
GET /v3/info
```

Response:

##### Info Response

| Field          | Type                               | Description                                        |
|----------------|------------------------------------|----------------------------------------------------|
| version        | [Version](#version-object) object  | The version of this Lavalink server                |
| builtTime      | int                                | The time this Lavalink jar was built               |
| git            | [Git](#git-object) object          | The git information of this Lavalink server        |
| jvm            | string                             | The JVM version this Lavalink server runs on       |
| lavaplayer     | string                             | The Lavaplayer version being used by this server   |
| sourceManagers | array of strings                   | The source manager names being used by this server |
| plugins        | array of [Plugins](#plugin-object) | The plugin names being used by this server         |

##### Version Object

| Field  | Type   | Description                               |
|--------|--------|-------------------------------------------|
| string | string | The full version of this Lavalink server  |
| major  | int    | The major version of this Lavalink server |
| minor  | int    | The minor version of this Lavalink server |
| patch  | int    | The patch version of this Lavalink server |

##### Git Object

| Field      | Type   | Description                             |
|------------|--------|-----------------------------------------|
| branch     | string | The branch of this Lavalink server      |
| commit     | string | The commit of this Lavalink server      |
| commitTime | string | The commit time of this Lavalink server |

##### Plugin Object

| Field   | Type   | Description               |
|---------|--------|---------------------------|
| name    | string | The name of the plugin    |
| version | string | The version of the plugin |

<details>
<summary>Example Payload</summary>

```json
{
  "version": {
    "string": "v3.6.0",
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
  ],
  "plugins": [
    {
      "name": "some-plugin",
      "version": "1.0.0"
    },
    {
      "name": "foo-plugin",
      "version": "1.2.3"
    }
  ]
}
```
</details>

---

#### Get Lavalink stats
Request Lavalink statistics.
```
GET /v3/stats
```

Response:

[Stats Object](#stats-object)

---

#### Get Plugins(DEPRECATED)
Request information about the plugins running on Lavalink, if any.
> `/plugins` is deprecated and marked for removal in v4
```
GET /plugins
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

---

#### Get Lavalink version(DEPRECATED)
Request Lavalink version.
> `/version` is deprecated and marked for removal in v4, use [`/info`](#get-lavalink-info) instead
```
GET /version
```

Response:
```
v3.6.0
```

---

### RoutePlanner API

Additionally, there are a few REST endpoints for the ip rotation extension

#### Get RoutePlanner status
> `/routeplanner/status` is deprecated and marked for removal in v4
```
GET /v3/routeplanner/status
```

Response:

| Field   | Type                                        | Description                                                           |
|---------|---------------------------------------------|-----------------------------------------------------------------------|
| class   | ?[Route Planner Type](#route-planner-types) | The name of the RoutePlanner implementation being used by this server |
| details | ?[Details](#details-object) object          | The status details of the RoutePlanner                                |

##### Route Planner Types

| Route Planner Type           | Description |
|------------------------------|-------------|
| `RotatingIpRoutePlanner`     | ...         |
| `NanoIpRoutePlanner`         | ...         |
| `RotatingNanoIpRoutePlanner` | ...         |

##### Details Object

| Field               | Type                                                  | Description                                                                           | Valid Types                                        |
|---------------------|-------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------|
| ipBlock             | [IP Block](#ip-block-object) object                   | The ip block being used                                                               | all                                                |
| failingAddresses    | array of [Failing Addresses](#failing-address-object) | The failing addresses                                                                 | all                                                |
| rotateIndex         | string                                                | The number of rotations                                                               | `RotatingIpRoutePlanner`                           |
| ipIndex             | string                                                | The current offset in the block                                                       | `RotatingIpRoutePlanner`                           |
| currentAddress      | string                                                | The current address being used                                                        | `RotatingIpRoutePlanner`                           |
| currentAddressIndex | string                                                | The current current offset in the ip block                                            | `NanoIpRoutePlanner`, `RotatingNanoIpRoutePlanner` |
| blockIndex          | string                                                | The information in which /64 block ips are chosen. This number increases on each ban. | `RotatingNanoIpRoutePlanner`                       |

##### IP Block Object

| Field | Type                            | Description              |
|-------|---------------------------------|--------------------------|
| type  | [IP Block Type](#ip-block-type) | The type of the ip block |
| size  | string                          | The size of the ip block |

##### IP Block Type

| IP Block Type  | Description         |
|----------------|---------------------|
| `Inet4Address` | The ipv4 block type |
| `Inet6Address` | The ipv6 block type |

##### Failing Address Object

| Field            | Type   | Description                                              |
|------------------|--------|----------------------------------------------------------|
| address          | string | The failing address                                      |
| failingTimestamp | int    | The timestamp when the address failed                    |
| failingTime      | string | The timestamp when the address failed as a pretty string |

<details>
<summary>Example Payload</summary>

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
</details>

---

#### Unmark a failed address
> `/routeplanner/free/address` is deprecated and marked for removal in v4
```
POST /v3/routeplanner/free/address
```

Request:

| Field   | Type   | Description                                                                 |
|---------|--------|-----------------------------------------------------------------------------|
| address | string | The address to unmark as failed. This address must be in the same ip block. |

<details>
<summary>Example Payload</summary>

```json
{
  "address": "1.0.0.1"
}
```
</details>

Response:

204 - No Content

---

#### Unmark all failed address
> `/routeplanner/free/all` is deprecated and marked for removal in v4
```
POST /v3/routeplanner/free/all
```

Response:

204 - No Content

---

### Resuming Lavalink sessions

What happens after your client disconnects is dependent on whether the session has been configured for resuming.

* If resuming is disabled all voice connections are closed immediately.
* If resuming is enabled all music will continue playing. You will then be able to resume your session, allowing you to control the players again.

To enable resuming, you must call the [Update Session](#update-session) endpoint with the `resumingKey` and `timeout`

<details>
<summary>Configure Resuming OP(DEPRECATED)</summary>

To enable resuming, you must send a `configureResuming` message.

* `key` is the string you will need to send when resuming the session. Set to null to disable resuming altogether. Defaults to null.
* `timeout` is the number of seconds after disconnecting before the session is closed anyway. This is useful for avoiding accidental leaks. Defaults to `60` (seconds).

```json
{
  "op": "configureResuming",
  "key": "myResumeKey",
  "timeout": 60
}
```
</details>

To resume a session, specify the resume key in your WebSocket handshake request headers:

```
Resume-Key: The resume key of the session you want to resume.
```

You can tell if your session was resumed by looking at the handshake response header `Session-Resumed` which is either `true` or `false`.
```
Session-Resumed: true
```

In case your websocket library doesn't support reading headers you can listen for the [ready op](#ready-op) and check the `resumed` field.

When a session is paused, any events that would normally have been sent is queued up. When the session is resumed, this
queue is then emptied and the events are then replayed. 

---

### Special notes

* When your shard's main WS connection dies, so does all your lavalink audio connections.
  * This also includes resumes

* If Lavalink-Server suddenly dies (think SIGKILL) the client will have to terminate any audio connections by sending this event:

```json
{"op":4,"d":{"self_deaf":false,"guild_id":"GUILD_ID_HERE","channel_id":null,"self_mute":false}}
```

---

<details>
<summary> Outgoing messages(DEPRECATED)</summary>

#### Provide a voice server update(DEPRECATED)

Provide an intercepted voice server update. This causes the server to connect to the voice channel.

```yaml
{
  "op": "voiceUpdate",
  "guildId": "...",
  "sessionId": "...",
  "event": { ... }
}
```

#### Play a track(DEPRECATED)

`startTime` is an optional setting that determines the number of milliseconds to offset the track by. Defaults to 0.

`endTime` is an optional setting that determines at the number of milliseconds at which point the track should stop playing. Helpful if you only want to play a snippet of a bigger track. By default, the track plays until its end as per the encoded data.

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
  // Example https://en.wikipedia.org/wiki/File:Fuse_Electronics_Tremolo_MK-III_Quick_Demo.ogv
  "tremolo": {
    "frequency": 2.0, // 0 < x
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Similar to tremolo. While tremolo oscillates the volume, vibrato oscillates the pitch.
  "vibrato": {
    "frequency": 2.0, // 0 < x ≤ 14
    "depth": 0.5      // 0 < x ≤ 1
  },
  
  // Rotates the sound around the stereo channels/user headphones aka Audio Panning. It can produce an effect similar to https://youtu.be/QB9EB8mTKcc (without the reverb)
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
</details>

---

# Common pitfalls
Admittedly Lavalink isn't inherently the most intuitive thing ever, and people tend to run into the same mistakes over again. Please double-check the following if you run into problems developing your client, and you can't connect to a voice channel or play audio:

1. Check that you are forwarding sendWS events to **Discord**.
2. Check that you are intercepting **VOICE_SERVER_UPDATE**s to **Lavalink**. Do not edit the event object from Discord.
3. Check that you aren't expecting to hear audio when you have forgotten to queue something up OR forgotten to join a voice channel.
4. Check that you are not trying to create a voice connection with your Discord library.
5. When in doubt, check the debug logfile at `/logs/debug.log`.
