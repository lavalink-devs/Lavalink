# Implementation guidelines

How to write your own client. The Java [Lavalink-Client](https://github.com/freyacodes/lavalink-client) will serve as an example implementation.
The Java client has support for JDA, but can also be adapted to work with other JVM libraries.

## Requirements

* You must be able to send messages via a shard's gateway connection.
* You must be able to intercept voice server & voice state updates from the gateway on your shard connection.

## Significant changes v3.7.0 -> v4.0.0

* removed all non version `/v3` or `/v4` endpoints (except `/version`).
* `/v4/websocket` does not accept any messages anymore.
* `v4` uses the `sessionId` instead of the `resumeKey` for resuming.
* `v4` now returns the tracks `artworkUrl` and `isrc` if the source supports it.
* removal of deprecated json fields like `track`.
* addition of `artworkUrl` and `isrc` fields to the [Track Info](#track-info) object.
* addition of the full [Track](#track) object in [TrackStartEvent](#trackstartevent), [TrackEndEvent](#trackendevent), [TrackExceptionEvent](#trackexceptionevent) and [TrackStuckEvent](#trackstuckevent).
* updated capitalization of [Track End Reason](#track-end-reason) and [Severity](#severity)
* reworked [Load Result](#track-loading-result) object

## Significant changes v3.6.0 -> v3.7.0

* Moved HTTP endpoints under the new `/v3` path with `/version` as the only exception.
* Deprecation of the old HTTP paths.
* WebSocket handshakes should be done with `/v3/websocket`. Handshakes on `/` are now deprecated.
* Deprecation of all client-to-server messages (play, stop, pause, seek, volume, filters, destroy, voiceUpdate & configureResuming).
* Addition of REST endpoints intended to replace client requests.
* Addition of new WebSocket dispatch [Ready OP](#ready-op) to get `sessionId` and `resume` status.
* Addition of new [Session](#update-session)/[Player](#get-player) REST API.
* Addition of `/v3/info`, replaces `/plugins`.
* Deprecation of `Track.track` in existing endpoints. Use `Track.encoded` instead.
* Deprecation of `TrackXEvent.track` in WebSocket dispatches. Use `TrackXEvent.encodedTrack` instead.
* Player now has a `state` field which contains the same structure as returned by the `playerUpdate` OP.

<details>
<summary>v3.7.0 Migration Guide</summary>

All websocket ops are deprecated as of `v3.7.0` and replaced with the following endpoints and json fields:

* `play` -> [Update Player Endpoint](#update-player) `track` or `identifier` field
* `stop` -> [Update Player Endpoint](#update-player) `track` field with `null`
* `pause` -> [Update Player Endpoint](#update-player) `pause` field
* `seek` -> [Update Player Endpoint](#update-player) `position` field
* `volume` -> [Update Player Endpoint](#update-player) `volume` field
* `filters` -> [Update Player Endpoint](#update-player) `filters` field
* `destroy` -> [Destroy Player Endpoint](#destroy-player)
* `voiceUpdate` -> [Update Player Endpoint](#update-player) `voice` field
* `configureResuming` -> [Update Session Endpoint](#update-session)

</details>

## Future breaking changes for v4

> **Warning**
> We are currently reconsidering these desicions. See https://github.com/lavalink-devs/Lavalink/discussions/859 for more info

* HTTP endpoints not under a version path (`/v3`, `/v4`) will be removed except `/version` in v4.
* `/v4/websocket` will not accept any websocket messages. In `v4` the websocket is only used for server-to-client messages.
* The `/v3` API will still be available to be used.

<details>
<summary>Older versions</summary>

## Significant changes v3.3 -> v3.4

* Added filters
* The `error` string on the `TrackExceptionEvent` has been deprecated and replaced by
  the [Exception](#exception-object) object following the same structure as the `LOAD_FAILED` error on [`/loadtracks`](#track-loading).
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
One problem we noticed is that since JDA is unaware of ratelimits on the bot's gateway connection, it would keep adding
to the ratelimit queue to the gateway. With this update this is now the responsibility of the Lavalink client or the
Discord client.

A voice connection is now initiated by forwarding a `voiceUpdate` (VOICE_SERVER_UPDATE) to the server. When you want to
disconnect or move to a different voice channel you must send Discord a new VOICE_STATE_UPDATE. If you want to move your
connection to a new Lavalink server you can simply send the VOICE_SERVER_UPDATE to the new node, and the other node
will be disconnected by Discord.

Depending on your Discord library, it may be possible to take advantage of the library's OP 4 handling. For instance,
the JDA client takes advantage of JDA's websocket write thread to send OP 4s for connects, disconnects and reconnects.

</details>

## Protocol

### Reference

Fields marked with `?` are optional and types marked with `?` are nullable.

### Opening a connection

You can establish a WebSocket connection against the path `/v4/websocket`.

When opening a websocket connection, you must supply 3 required headers:

| Header Name     | Description                                     |
|-----------------|-------------------------------------------------|
| `Authorization` | The password you set in your Lavalink config    |
| `User-Id`       | The user id of the bot                          |
| `Client-Name`   | The name of the client in `NAME/VERSION` format |
| `Session-Id`? * | The id of the previous session to resume        |

**\*For more information on resuming see [Resuming](#resuming-lavalink-sessions)**

<details>
<summary>Example Headers</summary>

```
Authorization: youshallnotpass
User-Id: 170939974227541168
Client-Name: lavalink-client/2.0.0
```

</details>

### Websocket Messages

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

| OP Type                           | Description                                                   |
|-----------------------------------|---------------------------------------------------------------|
| [ready](#ready-op)                | Dispatched when you successfully connect to the Lavalink node |
| [playerUpdate](#player-update-op) | Dispatched every x seconds with the latest player state       |
| [stats](#stats-op)                | Dispatched when the node sends stats once per minute          |
| [event](#event-op)                | Dispatched when player or voice events occur                  |

#### Ready OP

Dispatched by Lavalink upon successful connection and authorization. Contains fields determining if resuming was successful, as well as the session id.

| Field     | Type   | Description                                                                                    |
|-----------|--------|------------------------------------------------------------------------------------------------|
| resumed   | bool   | Whether this session was resumed                                                               |
| sessionId | string | The Lavalink session id of this connection. Not to be confused with a Discord voice session id |

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

Dispatched every x seconds (configurable in `application.yml`) with the current state of the player.

| Field   | Type                                 | Description                |
|---------|--------------------------------------|----------------------------|
| guildId | string                               | The guild id of the player |
| state   | [Player State](#player-state) object | The player state           |

##### Player State

| Field     | Type | Description                                                                              |
|-----------|------|------------------------------------------------------------------------------------------|
| time      | int  | Unix timestamp in milliseconds                                                           |
| position  | int  | The position of the track in milliseconds                                                |
| connected | bool | Whether Lavalink is connected to the voice gateway                                       |
| ping      | int  | The ping of the node to the Discord voice server in milliseconds (`-1` if not connected) |

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
    "ping": 50
  }
}
```

</details>

---

#### Stats OP

A collection of stats sent every minute.

##### Stats Object

| Field          | Type                                | Description                                                                                      |
|----------------|-------------------------------------|--------------------------------------------------------------------------------------------------|
| players        | int                                 | The amount of players connected to the node                                                      |
| playingPlayers | int                                 | The amount of players playing a track                                                            |
| uptime         | int                                 | The uptime of the node in milliseconds                                                           |
| memory         | [Memory](#memory) object            | The memory stats of the node                                                                     |
| cpu            | [CPU](#cpu) object                  | The cpu stats of the node                                                                        |
| frameStats     | ?[Frame Stats](#frame-stats) object | The frame stats of the node. `null` if the node has no players or when retrieved via `/v4/stats` |

##### Memory

| Field      | Type | Description                              |
|------------|------|------------------------------------------|
| free       | int  | The amount of free memory in bytes       |
| used       | int  | The amount of used memory in bytes       |
| allocated  | int  | The amount of allocated memory in bytes  |
| reservable | int  | The amount of reservable memory in bytes |

##### CPU

| Field        | Type  | Description                      |
|--------------|-------|----------------------------------|
| cores        | int   | The amount of cores the node has |
| systemLoad   | float | The system load of the node      |
| lavalinkLoad | float | The load of Lavalink on the node |

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

Server dispatched an event. See the [Event Types](#event-types) section for more information.

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

| Event Type                                    | Description                                                                 |
|-----------------------------------------------|-----------------------------------------------------------------------------|
| [TrackStartEvent](#trackstartevent)           | Dispatched when a track starts playing                                      |
| [TrackEndEvent](#trackendevent)               | Dispatched when a track ends                                                |
| [TrackExceptionEvent](#trackexceptionevent)   | Dispatched when a track throws an exception                                 |
| [TrackStuckEvent](#trackstuckevent)           | Dispatched when a track gets stuck while playing                            |
| [WebSocketClosedEvent](#websocketclosedevent) | Dispatched when the websocket connection to Discord voice servers is closed |

##### TrackStartEvent

Dispatched when a track starts playing.

| Field | Type                   | Description                    |
|-------|------------------------|--------------------------------|
| track | [Track](#track) object | The track that started playing |

<details>
<summary>Example Payload</summary>

```json
{
  "op": "event",
  "type": "TrackStartEvent",
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 0,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    },
    "pluginInfo": {}
  }
}
```

</details>

---

##### TrackEndEvent

Dispatched when a track ends.

| Field  | Type                                | Description                  |
|--------|-------------------------------------|------------------------------|
| track  | [Track](#track) object              | The track that ended playing |
| reason | [TrackEndReason](#track-end-reason) | The reason the track ended   |

##### Track End Reason

| Reason       | Description                | May Start Next |
|--------------|----------------------------|----------------|
| `finished`   | The track finished playing | true           |
| `loadFailed` | The track failed to load   | true           |
| `stopped`    | The track was stopped      | false          |
| `replaced`   | The track was replaced     | false          |
| `cleanup`    | The track was cleaned up   | false          |

<details>
<summary>Example Payload</summary>

```json
{
  "op": "event",
  "type": "TrackEndEvent",
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 0,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    },
    "pluginInfo": {}
  },
  "reason": "finished"
}
```

</details>

---

##### TrackExceptionEvent

Dispatched when a track throws an exception.

| Field     | Type                                  | Description                        |
|-----------|---------------------------------------|------------------------------------|
| track     | [Track](#track) object                | The track that threw the exception |
| exception | [Exception](#exception-object) object | The occurred exception             |

##### Exception Object

| Field    | Type                  | Description                   |
|----------|-----------------------|-------------------------------|
| message  | ?string               | The message of the exception  |
| severity | [Severity](#severity) | The severity of the exception |
| cause    | string                | The cause of the exception    |

##### Severity

| Severity     | Description                                                                                                                                                                                                                            |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common`     | The cause is known and expected, indicates that there is nothing wrong with the library itself                                                                                                                                         |
| `suspicious` | The cause might not be exactly known, but is possibly caused by outside factors. For example when an outside service responds in a format that we do not expect                                                                        |
| `fault`      | The probable cause is an issue with the library or there is no way to tell what the cause might be. This is the default level and other levels are used in cases where the thrower has more in-depth knowledge about the error |

<details>
<summary>Example Payload</summary>

```json
{
  "op": "event",
  "type": "TrackExceptionEvent",
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 0,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    },
    "pluginInfo": {}
  },
  "exception": {
    "message": "...",
    "severity": "common",
    "cause": "..."
  }
}
```

</details>

---

##### TrackStuckEvent

Dispatched when a track gets stuck while playing.

| Field       | Type                   | Description                                     |
|-------------|------------------------|-------------------------------------------------|
| track       | [Track](#track) object | The track that got stuck                        |
| thresholdMs | int                    | The threshold in milliseconds that was exceeded |

<details>
<summary>Example Payload</summary>

```json
{
  "op": "event",
  "type": "TrackStuckEvent",
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 0,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    },
    "pluginInfo": {}
  },
  "thresholdMs": 123456789
}
```

</details>

---

##### WebSocketClosedEvent

Dispatched when an audio WebSocket (to Discord) is closed.
This can happen for various reasons (normal and abnormal), e.g. when using an expired voice server update.
4xxx codes are usually bad.
See the [Discord Docs](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes).

| Field    | Type   | Description                                                                                                                       |
|----------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| code     | int    | The [Discord close event code](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes) |
| reason   | string | The close reason                                                                                                                  |
| byRemote | bool   | Whether the connection was closed by Discord                                                                                      |

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

### REST API

Lavalink exposes a REST API to allow for easy control of the players.
Most routes require the `Authorization` header with the configured password.

```
Authorization: youshallnotpass
```

Routes are prefixed with `/v3` as of `v3.7.0` and `/v4` as of `v4.0.0`. Routes without an API prefix were removed in v4 (except `/version`).

#### Error Responses

When Lavalink encounters an error, it will respond with a JSON object containing more information about the error. Include the `trace=true` query param to also receive the full stack trace.

| Field     | Type   | Description                                                                 |
|-----------|--------|-----------------------------------------------------------------------------|
| timestamp | int    | The timestamp of the error in milliseconds since the Unix epoch             |
| status    | int    | The HTTP status code                                                        |
| error     | string | The HTTP status code message                                                |
| trace?    | string | The stack trace of the error when `trace=true` as query param has been sent |
| message   | string | The error message                                                           |
| path      | string | The request path                                                            |

<details>
<summary>Example Payload</summary>

```json
{
  "timestamp": 1667857581613,
  "status": 404,
  "error": "Not Found",
  "trace": "...",
  "message": "Session not found",
  "path": "/v4/sessions/xtaug914v9k5032f/players/817327181659111454"
}
```

</details>

#### Get Players

Returns a list of players in this specific session.

```
GET /v4/sessions/{sessionId}/players
```

##### Player

| Field   | Type                                 | Description                                           |
|---------|--------------------------------------|-------------------------------------------------------|
| guildId | string                               | The guild id of the player                            |
| track   | ?[Track](#track) object              | The currently playing track                           |
| volume  | int                                  | The volume of the player, range 0-1000, in percentage |
| paused  | bool                                 | Whether the player is paused                          |
| state   | [Player State](#player-state) object | The state of the player                               |
| voice   | [Voice State](#voice-state) object   | The voice state of the player                         |
| filters | [Filters](#filters) object           | The filters used by the player                        |              

##### Track

| Field      | Type                             | Description                             |
|------------|----------------------------------|-----------------------------------------|
| encoded    | string                           | The base64 encoded track data           |
| info       | [Track Info](#track-info) object | Info about the track                    |
| pluginInfo | object                           | Addition track info provided by plugins |

##### Track Info

| Field      | Type    | Description                                                                           |
|------------|---------|---------------------------------------------------------------------------------------|
| identifier | string  | The track identifier                                                                  |
| isSeekable | bool    | Whether the track is seekable                                                         |
| author     | string  | The track author                                                                      |
| length     | int     | The track length in milliseconds                                                      |
| isStream   | bool    | Whether the track is a stream                                                         |
| position   | int     | The track position in milliseconds                                                    |
| title      | string  | The track title                                                                       |
| uri        | ?string | The track uri                                                                         |
| artworkUrl | ?string | The track artwork url                                                                 |
| isrc       | ?string | The track [ISRC](https://en.wikipedia.org/wiki/International_Standard_Recording_Code) |
| sourceName | string  | The track source name                                                                 |

##### Voice State

| Field      | Type   | Description                                                                                 |
|------------|--------|---------------------------------------------------------------------------------------------|
| token      | string | The Discord voice token to authenticate with                                                |
| endpoint   | string | The Discord voice endpoint to connect to                                                    |
| sessionId  | string | The Discord voice session id to authenticate with                                           |

`token`, `endpoint`, and `sessionId` are the 3 required values for connecting to one of Discord's voice servers.
`sessionId` is provided by the Voice State Update event sent by Discord, whereas the `endpoint` and `token` are provided
with the Voice Server Update. Please refer to https://discord.com/developers/docs/topics/gateway-events#voice

<details>
<summary>Example Payload</summary>

```yaml
[
  {
    "guildId": "...",
    "track": {
      "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
      "info": {
        "identifier": "dQw4w9WgXcQ",
        "isSeekable": true,
        "author": "RickAstleyVEVO",
        "length": 212000,
        "isStream": false,
        "position": 60000,
        "title": "Rick Astley - Never Gonna Give You Up",
        "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
        "isrc": null,
        "sourceName": "youtube"
      },
      "pluginInfo": {}
    },
    "volume": 100,
    "paused": false,
    "state": {
      "time": 1500467109,
      "position": 60000,
      "connected": true,
      "ping": 50
    },
    "voice": {
      "token": "...",
      "endpoint": "...",
      "sessionId": "..."
    },
    "filters": { ... }
  },
  ...
]
```

</details>

---

#### Get Player

Returns the player for this guild in this session.

```
GET /v4/sessions/{sessionId}/players/{guildId}
```

Response:

[Player](#Player) object

<details>
<summary>Example Payload</summary>

```yaml
{
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 60000,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    }
  },
  "volume": 100,
  "paused": false,
  "state": {
    "time": 1500467109,
    "position": 60000,
    "connected": true,
    "ping": 50
  },
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "..."
  },
  "filters": { ... }
}
```

</details>

---

#### Update Player

Updates or creates the player for this guild if it doesn't already exist.

```
PATCH /v4/sessions/{sessionId}/players/{guildId}?noReplace=true
```

Query Params:

| Field      | Type | Description                                                                  |
|------------|------|------------------------------------------------------------------------------|
| noReplace? | bool | Whether to replace the current track with the new track. Defaults to `false` |

Request:

| Field           | Type                               | Description                                                                                   |
|-----------------|------------------------------------|-----------------------------------------------------------------------------------------------|
| encodedTrack? * | ?string                            | The base64 encoded track to play. `null` stops the current track                              |
| identifier? *   | string                             | The identifier of the track to play                                                           |
| position?       | int                                | The track position in milliseconds                                                            |
| endTime?        | ?int                               | The track end time in milliseconds (must be > 0). `null` resets this if it was set previously |
| volume?         | int                                | The player volume, in percentage, from 0 to 1000                                              |
| paused?         | bool                               | Whether the player is paused                                                                  |
| filters?        | [Filters](#filters) object         | The new filters to apply. This will override all previously applied filters                   |                   
| voice?          | [Voice State](#voice-state) object | Information required for connecting to Discord                                                |

> **Note**
> - \* `encodedTrack` and `identifier` are mutually exclusive.
> - `sessionId` in the path should be the value from the [ready op](#ready-op).

When `identifier` is used, Lavalink will try to resolve the identifier as a single track. An HTTP `400` error is returned when resolving a playlist, search result, or no tracks.

<details>
<summary>Example Payload</summary>

```yaml
{
  "encodedTrack": "...",
  "identifier": "...",
  "startTime": 0,
  "endTime": 0,
  "volume": 100,
  "position": 32400,
  "paused": false,
  "filters": { ... },
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "..."
  }
}
```

</details>

Response:

[Player](#Player) object

<details>
<summary>Example Payload</summary>

```yaml
{
  "guildId": "...",
  "track": {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 60000,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    }
  },
  "volume": 100,
  "paused": false,
  "state": {
    "time": 1500467109,
    "position": 60000,
    "connected": true,
    "ping": 50         
  },
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "..."
  },
  "filters": { ... }
}
```

</details>

---

#### Filters

Filters are used in above requests and look like this

| Field       | Type                                      | Description                                                                                  |
|-------------|-------------------------------------------|----------------------------------------------------------------------------------------------|
| volume?     | float                                     | Adjusts the player volume from 0.0 to 5.0, where 1.0 is 100%. Values >1.0 may cause clipping |
| equalizer?  | array of [Equalizer](#equalizer) objects  | Adjusts 15 different bands                                                                   |
| karaoke?    | [Karaoke](#karaoke) object                | Eliminates part of a band, usually targeting vocals                                           |
| timescale?  | [Timescale](#timescale) object            | Changes the speed, pitch, and rate                                                           |
| tremolo?    | [Tremolo](#tremolo) object                | Creates a shuddering effect, where the volume quickly oscillates                             |
| vibrato?    | [Vibrato](#vibrato) object                | Creates a shuddering effect, where the pitch quickly oscillates                              |
| rotation?   | [Rotation](#rotation) object              | Rotates the audio around the stereo channels/user headphones (aka Audio Panning)             |
| distortion? | [Distortion](#distortion) object          | Distorts the audio                                                                           |
| channelMix? | [Channel Mix](#channel-mix) object        | Mixes both channels (left and right)                                                         |
| lowPass?    | [Low Pass](#low-pass) object              | Filters higher frequencies                                                                   |
| ...         | ...                                       | Plugins may add different filters which can also be set here                                 |

##### Equalizer

There are 15 bands (0-14) that can be changed.
"gain" is the multiplier for the given band. The default value is 0. Valid values range from -0.25 to 1.0,
where -0.25 means the given band is completely muted, and 0.25 means it is doubled. Modifying the gain could also change the volume of the output.

<details>
<summary>Band Frequencies</summary>

| Band | Frequency |
|------|-----------|
| 0    | 25 Hz     |
| 1    | 40 Hz     |
| 2    | 63 Hz     |
| 3    | 100 Hz    |
| 4    | 160 Hz    |
| 5    | 250 Hz    |
| 6    | 400 Hz    |
| 7    | 630 Hz    |
| 8    | 1000 Hz   |
| 9    | 1600 Hz   |
| 10   | 2500 Hz   |
| 11   | 4000 Hz   |
| 12   | 6300 Hz   |
| 13   | 10000 Hz  |
| 14   | 16000 Hz  |

</details>

| Field | Type  | Description             |
|-------|-------|-------------------------|
| band  | int   | The band (0 to 14)      |
| gain  | float | The gain (-0.25 to 1.0) |

##### Karaoke

Uses equalization to eliminate part of a band, usually targeting vocals.

| Field        | Type  | Description                                                             |
|--------------|-------|-------------------------------------------------------------------------|
| level?       | float | The level (0 to 1.0 where 0.0 is no effect and 1.0 is full effect)      |
| monoLevel?   | float | The mono level (0 to 1.0 where 0.0 is no effect and 1.0 is full effect) |
| filterBand?  | float | The filter band (in Hz)                                                 |
| filterWidth? | float | The filter width                                                        |

##### Timescale

Changes the speed, pitch, and rate. All default to 1.0.

| Field  | Type  | Description                |
|--------|-------|----------------------------|
| speed? | float | The playback speed 0.0 ≤ x |
| pitch? | float | The pitch 0.0 ≤ x          |
| rate?  | float | The rate 0.0 ≤ x           |

##### Tremolo

Uses amplification to create a shuddering effect, where the volume quickly oscillates.
Demo: https://en.wikipedia.org/wiki/File:Fuse_Electronics_Tremolo_MK-III_Quick_Demo.ogv

| Field      | Type  | Description                     |
|------------|-------|---------------------------------|
| frequency? | float | The frequency 0.0 < x           |
| depth?     | float | The tremolo depth 0.0 < x ≤ 1.0 |

##### Vibrato

Similar to tremolo. While tremolo oscillates the volume, vibrato oscillates the pitch.

| Field      | Type  | Description                     |
|------------|-------|---------------------------------|
| frequency? | float | The frequency 0.0 < x ≤ 14.0    |
| depth?     | float | The vibrato depth 0.0 < x ≤ 1.0 |

##### Rotation

Rotates the sound around the stereo channels/user headphones (aka Audio Panning). It can produce an effect similar to https://youtu.be/QB9EB8mTKcc (without the reverb).

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
Any smoothing values equal to or less than 1.0 will disable the filter.

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
DELETE /v4/sessions/{sessionId}/players/{guildId}
```

Response:

204 - No Content

---

#### Update Session

Updates the session with the resuming state and timeout.

```
PATCH /v4/sessions/{sessionId}
```

Request:

| Field     | Type | Description                                         |
|-----------|------|-----------------------------------------------------|
| resuming? | bool | Whether resuming is enabled for this session or not |
| timeout?  | int  | The timeout in seconds (default is 60s)             |

<details>
<summary>Example Payload</summary>

```json
{
  "resuming": false,
  "timeout": 0
}
```

</details>

Response:

| Field    | Type | Description                                         |
|----------|------|-----------------------------------------------------|
| resuming | bool | Whether resuming is enabled for this session or not |
| timeout  | int  | The timeout in seconds (default is 60s)             |

<details>
<summary>Example Payload</summary>

```json
{
  "resuming": true,
  "timeout": 60
}
```

</details>

---

#### Track Loading

This endpoint is used to resolve audio tracks for use with the [Update Player](#update-player) endpoint.

```
GET /v4/loadtracks?identifier=dQw4w9WgXcQ
```

Response:

##### Track Loading Result

| Field    | Type                                | Description            |       
|----------|-------------------------------------|------------------------|
| loadType | [LoadResultType](#load-result-type) | The type of the result | 
| data     | [LoadResultData](#load-result-data) | The data of the result |

##### Load Result Type

| Load Result Type | Description                                   |
|------------------|-----------------------------------------------|
| `track`          | A track has been loaded                       |
| `playlist`       | A playlist has been loaded                    |
| `search`         | A search result has been loaded               |
| `empty`          | There has been no matches for your identifier |
| `error`          | Loading has failed with an error              |

##### Load Result Data

###### Load Result Data - Track

[Track](#track) object with the loaded track.

<details>
<summary>Example Payload</summary>

```yaml
{
  "loadType": "track",
  "data": {
    "encoded": "...",
    "info": { ... },
    "pluginInfo": { ... }
  }
}
```

</details>

###### Load Result Data - Playlist

| Field      | Type                                  | Description                                 |
|------------|---------------------------------------|---------------------------------------------|
| info       | [PlaylistInfo](#playlist-info) object | The info of the playlist                    |
| pluginInfo | Object                                | Addition playlist info provided by plugins  |
| tracks     | array of [Track](#track) objects      | The tracks of the playlist                  |

###### Playlist Info

| Field         | Type   | Description                                                     |
|---------------|--------|-----------------------------------------------------------------|
| name          | string | The name of the playlist                                        |
| selectedTrack | int    | The selected track of the playlist (-1 if no track is selected) |

<details>
<summary>Example Payload</summary>

```yaml
{
  "loadType": "playlist",
  "data": {
    "info": { ... },
    "pluginInfo": { ... },
    "tracks": [ ... ]
  }
}
```

</details>

###### Load Result Data - Search

Array of [Track](#track) objects from the search result.

<details>
<summary>Example Payload</summary>

```yaml
{
  "loadType": "search",
  "data": [
    {
      "encoded": "...",
      "info": { ... },
      "pluginInfo": { ... }
    },
    ...
  ]
}
```

</details>

###### Load Result Data - Empty

Empty object.

<details>
<summary>Example Payload</summary>

```yaml
{
  "loadType": "empty",
  "data": {}
}
```

</details>

###### Load Result Data - Error

[Exception](#exception-object) object with the error.

<details>
<summary>Example Payload</summary>

```yaml
{
  "loadType": "error",
  "data": { 
    "message": "Something went wrong",
    "severity": "fatal",
    "cause": "..."
  }
}
```

</details>

---

#### Track Searching

Lavalink supports searching via YouTube, YouTube Music, and Soundcloud. To search, you must prefix your identifier with `ytsearch:`, `ytmsearch:` or `scsearch:` respectively.

When a search prefix is used, the returned `loadType` will be `SEARCH_RESULT`. Note that disabling the respective source managers renders these search prefixes useless. Plugins may also implement prefixes to allow for more search engines to be utilised.

---

#### Track Decoding

Decode a single track into its info, where `BASE64` is the encoded base64 data.

```
GET /v4/decodetrack?encodedTrack=BASE64
```

Response:

[Track](#track) object

<details>
<summary>Example Payload</summary>

```yaml
{
  "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
  "info": {
    "identifier": "dQw4w9WgXcQ",
    "isSeekable": true,
    "author": "RickAstleyVEVO",
    "length": 212000,
    "isStream": false,
    "position": 0,
    "title": "Rick Astley - Never Gonna Give You Up",
    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
    "isrc": null,
    "sourceName": "youtube"
  },
  "pluginInfo": {}
}
```

</details>

---

Decodes multiple tracks into their info

```
POST /v4/decodetracks
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

Array of [Track](#track) objects

<details>
<summary>Example Payload</summary>

```yaml
[
  {
    "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
    "info": {
      "identifier": "dQw4w9WgXcQ",
      "isSeekable": true,
      "author": "RickAstleyVEVO",
      "length": 212000,
      "isStream": false,
      "position": 0,
      "title": "Rick Astley - Never Gonna Give You Up",
      "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
      "isrc": null,
      "sourceName": "youtube"
    },
    "pluginInfo": {}
  },
  ...
]
```

</details>

---

#### Get Lavalink info

Request Lavalink information.

```
GET /v4/info
```

Response:

##### Info Response

| Field          | Type                                      | Description                                                     |
|----------------|-------------------------------------------|-----------------------------------------------------------------|
| version        | [Version](#version-object) object         | The version of this Lavalink server                             |
| buildTime      | int                                       | The millisecond unix timestamp when this Lavalink jar was built |
| git            | [Git](#git-object) object                 | The git information of this Lavalink server                     |
| jvm            | string                                    | The JVM version this Lavalink server runs on                    |
| lavaplayer     | string                                    | The Lavaplayer version being used by this server                |
| sourceManagers | array of strings                          | The enabled source managers for this server                     |
| filters        | array of strings                          | The enabled filters for this server                             |
| plugins        | array of [Plugin](#plugin-object) objects | The enabled plugins for this server                             |

##### Version Object

Parsed Semantic Versioning 2.0.0. See https://semver.org/ for more info

| Field      | Type    | Description                                                                        |
|------------|---------|------------------------------------------------------------------------------------|
| semver     | string  | The full version string of this Lavalink server                                    |
| major      | int     | The major version of this Lavalink server                                          |
| minor      | int     | The minor version of this Lavalink server                                          |
| patch      | int     | The patch version of this Lavalink server                                          |
| preRelease | ?string | The pre-release version according to semver as a `.` separated list of identifiers |
| build      | ?string | The build metadata according to semver as a `.` separated list of identifiers      |

##### Git Object

| Field      | Type   | Description                                                    |
|------------|--------|----------------------------------------------------------------|
| branch     | string | The branch this Lavalink server was built                      |
| commit     | string | The commit this Lavalink server was built                      |
| commitTime | int    | The millisecond unix timestamp for when the commit was created |

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
    "string": "3.7.0-rc.1+test",
    "major": 3,
    "minor": 7,
    "patch": 0,
    "preRelease": "rc.1",
    "build": "test"
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
  "filters": [
    "equalizer",
    "karaoke",
    "timescale",
    "channelMix"
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
GET /v4/stats
```

Response:

`frameStats` is always missing for this endpoint.
[Stats](#stats-object) object

<details>
<summary>Example Payload</summary>

```json
{
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
  }
}
```

</details>

---

#### Get Lavalink version

Request Lavalink version.

```
GET /version
```

Response:

```
4.0.0
```

---

### RoutePlanner API

Additionally, there are a few REST endpoints for the ip rotation extension.

#### Get RoutePlanner status

```
GET /v4/routeplanner/status
```

Response:

| Field   | Type                                        | Description                                                           |
|---------|---------------------------------------------|-----------------------------------------------------------------------|
| class   | ?[Route Planner Type](#route-planner-types) | The name of the RoutePlanner implementation being used by this server |
| details | ?[Details](#details-object) object          | The status details of the RoutePlanner                                |

##### Route Planner Types

| Route Planner Type           | Description                                                                                                                 |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `RotatingIpRoutePlanner`     | IP address used is switched on ban. Recommended for IPv4 blocks or IPv6 blocks smaller than a /64.                          |
| `NanoIpRoutePlanner`         | IP address used is switched on clock update. Use with at least 1 /64 IPv6 block.                                            |
| `RotatingNanoIpRoutePlanner` | IP address used is switched on clock update, rotates to a different /64 block on ban. Use with at least 2x /64 IPv6 blocks. |
| `BalancingIpRoutePlanner`    | IP address used is selected at random per request. Recommended for larger IP blocks.                                        |

##### Details Object

| Field               | Type                                                  | Description                                                                           | Valid Types                                        |
|---------------------|-------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------|
| ipBlock             | [IP Block](#ip-block-object) object                   | The ip block being used                                                               | all                                                |
| failingAddresses    | array of [Failing Addresses](#failing-address-object) | The failing addresses                                                                 | all                                                |
| rotateIndex         | string                                                | The number of rotations                                                               | `RotatingIpRoutePlanner`                           |
| ipIndex             | string                                                | The current offset in the block                                                       | `RotatingIpRoutePlanner`                           |
| currentAddress      | string                                                | The current address being used                                                        | `RotatingIpRoutePlanner`                           |
| currentAddressIndex | string                                                | The current offset in the ip block                                                    | `NanoIpRoutePlanner`, `RotatingNanoIpRoutePlanner` |
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
| failingAddress   | string | The failing address                                      |
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
        "failingAddress": "/1.0.0.0",
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

```
POST /v4/routeplanner/free/address
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

```
POST /v4/routeplanner/free/all
```

Response:

204 - No Content

---

### Resuming Lavalink Sessions

What happens after your client disconnects is dependent on whether the session has been configured for resuming.

* If resuming is disabled all voice connections are closed immediately.
* If resuming is enabled all music will continue playing. You will then be able to resume your session, allowing you to control the players again.

To enable resuming, you must call the [Update Session](#update-session) endpoint with the `resuming` and `timeout`.

To resume a session, specify the session id in your WebSocket handshake request headers:

```
Session-Id: The id of the session you want to resume.
```

You can tell if your session was resumed by looking at the handshake response header `Session-Resumed` which is either `true` or `false`.

```
Session-Resumed: true
```

In case your websocket library doesn't support reading headers you can listen for the [ready op](#ready-op) and check the `resumed` field.

When a session is paused, any events that would normally have been sent are queued up. When the session is resumed, this
queue is then emptied and the events are replayed.

---

### Special notes

* When your shard's main WS connection dies, so does all your Lavalink audio connections
  * This also includes resumes

* If Lavalink-Server suddenly dies (think SIGKILL) the client will have to terminate any audio connections by sending this event to Discord:

```json
{
  "op": 4,
  "d": {
    "self_deaf": false,
    "guild_id": "GUILD_ID_HERE",
    "channel_id": null,
    "self_mute": false
  }
}
```

---

# Common pitfalls

Admittedly Lavalink isn't inherently the most intuitive thing ever, and people tend to run into the same mistakes over again. Please double-check the following if you run into problems developing your client, and you can't connect to a voice channel or play audio:

1. Check that you are intercepting **VOICE_SERVER_UPDATE**s and **VOICE_STATE_UPDATE**s to **Lavalink**. You only need the `endpoint`, `token`, and `session_id`.
2. Check that you aren't expecting to hear audio when you have forgotten to queue something up OR forgotten to join a voice channel.
3. Check that you are not trying to create a voice connection with your Discord library.
4. When in doubt, check the debug logfile at `/logs/debug.log`.
