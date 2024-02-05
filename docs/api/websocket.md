---
description: Lavalink WebSocket API documentation.
---

# WebSocket API

## Opening a connection

You can establish a WebSocket connection against the path `/v4/websocket`.

When opening a websocket connection, you must supply 3 required headers:

| Header Name     | Description                                     |
|-----------------|-------------------------------------------------|
| `Authorization` | The password you set in your Lavalink config    |
| `User-Id`       | The user id of the bot                          |
| `Client-Name`   | The name of the client in `NAME/VERSION` format |
| `Session-Id`? * | The id of the previous session to resume        |

**\*For more information on resuming see [Resuming](index.md#resuming)**

<details markdown="1">
<summary>Example Headers</summary>

```
Authorization: youshallnotpass
User-Id: 170939974227541168
Client-Name: lavalink-client/2.0.0
```

</details>

Websocket messages all follow the following standard format:

| Field | Type                 | Description                           |
|-------|----------------------|---------------------------------------|
| op    | [OP Type](#op-types) | The op type                           |
| ...   | ...                  | Extra fields depending on the op type |

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "op": "...",
  ...
}
```

</details>

## OP Types

| OP Type                           | Description                                                   |
|-----------------------------------|---------------------------------------------------------------|
| [ready](#ready-op)                | Dispatched when you successfully connect to the Lavalink node |
| [playerUpdate](#player-update-op) | Dispatched every x seconds with the latest player state       |
| [stats](#stats-op)                | Dispatched when the node sends stats once per minute          |
| [event](#event-op)                | Dispatched when player or voice events occur                  |

### Ready OP

Dispatched by Lavalink upon successful connection and authorization. Contains fields determining if resuming was successful, as well as the session id.

| Field     | Type   | Description                                                                                    |
|-----------|--------|------------------------------------------------------------------------------------------------|
| resumed   | bool   | Whether this session was resumed                                                               |
| sessionId | string | The Lavalink session id of this connection. Not to be confused with a Discord voice session id |

<details markdown="1">
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

### Player Update OP

Dispatched every x seconds (configurable in `application.yml`) with the current state of the player.

| Field   | Type                                 | Description                |
|---------|--------------------------------------|----------------------------|
| guildId | string                               | The guild id of the player |
| state   | [Player State](#player-state) object | The player state           |

### Player State

| Field     | Type | Description                                                                              |
|-----------|------|------------------------------------------------------------------------------------------|
| time      | int  | Unix timestamp in milliseconds                                                           |
| position  | int  | The position of the track in milliseconds                                                |
| connected | bool | Whether Lavalink is connected to the voice gateway                                       |
| ping      | int  | The ping of the node to the Discord voice server in milliseconds (`-1` if not connected) |

<details markdown="1">
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

### Stats OP

A collection of statistics sent every minute.

#### Stats Object

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

| Field     | Type | Description                                                          |
|-----------|------|----------------------------------------------------------------------|
| sent      | int  | The amount of frames sent to Discord                                 |
| nulled    | int  | The amount of frames that were nulled                                |
| deficit * | int  | The difference between sent frames and the expected amount of frames |

\* The expected amount of frames is 3000 (1 every 20 ms) per player. If the `deficit` is negative, too many frames were sent, and if it's positive, not enough frames got sent.

<details markdown="1">
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
    "sent": 6000,
    "nulled": 10,
    "deficit": -3010
  }
}
```

</details>

---

### Event OP

Server dispatched an event. See the [Event Types](#event-types) section for more information.

| Field   | Type                      | Description                         |
|---------|---------------------------|-------------------------------------|
| type    | [EventType](#event-types) | The type of event                   |
| guildId | string                    | The guild id                        |
| ...     | ...                       | Extra fields depending on the event |

<details markdown="1">
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

#### TrackStartEvent

Dispatched when a track starts playing.

| Field | Type                          | Description                    |
|-------|-------------------------------|--------------------------------|
| track | [Track](rest.md#track) object | The track that started playing |

<details markdown="1">
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

#### TrackEndEvent

Dispatched when a track ends.

| Field  | Type                                | Description                  |
|--------|-------------------------------------|------------------------------|
| track  | [Track](rest.md#track) object       | The track that ended playing |
| reason | [TrackEndReason](#track-end-reason) | The reason the track ended   |

##### Track End Reason

| Reason       | Description                | May Start Next |
|--------------|----------------------------|----------------|
| `finished`   | The track finished playing | true           |
| `loadFailed` | The track failed to load   | true           |
| `stopped`    | The track was stopped      | false          |
| `replaced`   | The track was replaced     | false          |
| `cleanup`    | The track was cleaned up   | false          |

<details markdown="1">
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

#### TrackExceptionEvent

Dispatched when a track throws an exception.

| Field     | Type                                  | Description                        |
|-----------|---------------------------------------|------------------------------------|
| track     | [Track](rest.md#track) object         | The track that threw the exception |
| exception | [Exception](#exception-object) object | The occurred exception             |

##### Exception Object

| Field    | Type                  | Description                   |
|----------|-----------------------|-------------------------------|
| message  | ?string               | The message of the exception  |
| severity | [Severity](#severity) | The severity of the exception |
| cause    | string                | The cause of the exception    |

##### Severity

| Severity     | Description                                                                                                                                                                                                                    |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `common`     | The cause is known and expected, indicates that there is nothing wrong with the library itself                                                                                                                                 |
| `suspicious` | The cause might not be exactly known, but is possibly caused by outside factors. For example when an outside service responds in a format that we do not expect                                                                |
| `fault`      | The probable cause is an issue with the library or there is no way to tell what the cause might be. This is the default level and other levels are used in cases where the thrower has more in-depth knowledge about the error |

<details markdown="1">
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

#### TrackStuckEvent

Dispatched when a track gets stuck while playing.

| Field       | Type                          | Description                                     |
|-------------|-------------------------------|-------------------------------------------------|
| track       | [Track](rest.md#track) object | The track that got stuck                        |
| thresholdMs | int                           | The threshold in milliseconds that was exceeded |

<details markdown="1">
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

#### WebSocketClosedEvent

Dispatched when an audio WebSocket (to Discord) is closed.
This can happen for various reasons (normal and abnormal), e.g. when using an expired voice server update.
4xxx codes are usually bad.
See the [Discord Docs](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes).

| Field    | Type   | Description                                                                                                                       |
|----------|--------|-----------------------------------------------------------------------------------------------------------------------------------|
| code     | int    | The [Discord close event code](https://discord.com/developers/docs/topics/opcodes-and-status-codes#voice-voice-close-event-codes) |
| reason   | string | The close reason                                                                                                                  |
| byRemote | bool   | Whether the connection was closed by Discord                                                                                      |

<details markdown="1">
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