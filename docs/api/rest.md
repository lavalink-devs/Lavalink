---
description: Lavalink REST API documentation.
---

# REST API

Lavalink exposes a REST API to allow for easy control of the players.
Most routes require the `Authorization` header with the configured password.

```
Authorization: youshallnotpass
```

Routes are prefixed with `/v3` as of `v3.7.0` and `/v4` as of `v4.0.0`. Routes without an API prefix were removed in v4 (except `/version`).

## Insomnia Collection

You can find an [Insomnia](https://insomnia.rest/) collection in the [here](Insomnia.json) which contains all the endpoints and their respective payloads.

## Error Responses

When Lavalink encounters an error, it will respond with a JSON object containing more information about the error. Include the `trace=true` query param to also receive the full stack trace.

| Field     | Type   | Description                                                                 |
|-----------|--------|-----------------------------------------------------------------------------|
| timestamp | int    | The timestamp of the error in milliseconds since the Unix epoch             |
| status    | int    | The HTTP status code                                                        |
| error     | string | The HTTP status code message                                                |
| trace?    | string | The stack trace of the error when `trace=true` as query param has been sent |
| message   | string | The error message                                                           |
| path      | string | The request path                                                            |

<details markdown="1">
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


## Track API

### Common Types  ### {: #track-api-types }

#### Track

| Field      | Type                             | Description                                                                     |
|------------|----------------------------------|---------------------------------------------------------------------------------|
| encoded    | string                           | The base64 encoded track data                                                   |
| info       | [Track Info](#track-info) object | Info about the track                                                            |
| pluginInfo | object                           | Additional track info provided by plugins                                       |
| userData   | object                           | Additional track data provided via the [Update Player](#update-player) endpoint |

#### Track Info

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

#### Playlist Info

| Field         | Type   | Description                                                     |
|---------------|--------|-----------------------------------------------------------------|
| name          | string | The name of the playlist                                        |
| selectedTrack | int    | The selected track of the playlist (-1 if no track is selected) |

---

### Track Loading

This endpoint is used to resolve audio tracks for use with the [Update Player](#update-player) endpoint.


!!! tip
    
    Lavalink supports searching via YouTube, YouTube Music, and Soundcloud. To search, you must prefix your identifier with `ytsearch:`, `ytmsearch:` or `scsearch:` respectively.
    
    When a search prefix is used, the returned `loadType` will be `search`. Note that disabling the respective source managers renders these search prefixes useless.

    Plugins may also implement prefixes to allow for more search engines to be utilised.


```
GET /v4/loadtracks?identifier=dQw4w9WgXcQ
```

Response:

#### Track Loading Result

| Field    | Type                                | Description            |       
|----------|-------------------------------------|------------------------|
| loadType | [LoadResultType](#load-result-type) | The type of the result | 
| data     | [LoadResultData](#load-result-data) | The data of the result |

#### Load Result Type

| Load Result Type | Description                                   |
|------------------|-----------------------------------------------|
| `track`          | A track has been loaded                       |
| `playlist`       | A playlist has been loaded                    |
| `search`         | A search result has been loaded               |
| `empty`          | There has been no matches for your identifier |
| `error`          | Loading has failed with an error              |

#### Load Result Data

##### Track Result Data

[Track](#track) object with the loaded track.

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "loadType": "track",
  "data": {
    "encoded": "...",
    "info": { ... },
    "pluginInfo": { ... },
    "userData": { ... }
  }
}
```

</details>

##### Playlist Result Data

| Field      | Type                                  | Description                                |
|------------|---------------------------------------|--------------------------------------------|
| info       | [PlaylistInfo](#playlist-info) object | The info of the playlist                   |
| pluginInfo | Object                                | Addition playlist info provided by plugins |
| tracks     | array of [Track](#track) objects      | The tracks of the playlist                 |

<details markdown="1">
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

##### Search Result Data

Array of [Track](#track) objects from the search result.

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "loadType": "search",
  "data": [
    {
      "encoded": "...",
      "info": { ... },
      "pluginInfo": { ... },
      "userData": { ... }
    },
    ...
  ]
}
```

</details>

##### Empty Result Data

Empty object.

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "loadType": "empty",
  "data": {}
}
```

</details>

##### Error Result Data

[Exception](websocket.md#exception-object) object with the error.

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "loadType": "error",
  "data": { 
    "message": "Something went wrong",
    "severity": "fault",
    "cause": "..."
  }
}
```

</details>

---

### Track Decoding

Decode a single track into its info, where `BASE64` is the encoded base64 data.

```
GET /v4/decodetrack?encodedTrack=BASE64
```

Response:

[Track](#track) object

<details markdown="1">
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
  "pluginInfo": { ... },
  "userData": { ... }
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

<details markdown="1">
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

<details markdown="1">
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
    "pluginInfo": { ... },
    "userData": { ... }
  },
  ...
]
```

</details>

---

## Player API

### Common Types  ### {: #player-api-types }

#### Player

| Field   | Type                                             | Description                                           |
|---------|--------------------------------------------------|-------------------------------------------------------|
| guildId | string                                           | The guild id of the player                            |
| track   | ?[Track](#track) object                          | The currently playing track                           |
| volume  | int                                              | The volume of the player, range 0-1000, in percentage |
| paused  | bool                                             | Whether the player is paused                          |
| state   | [Player State](websocket.md#player-state) object | The state of the player                               |
| voice   | [Voice State](#voice-state) object               | The voice state of the player                         |
| filters | [Filters](#filters) object                       | The filters used by the player                        |

#### Voice State

| Field     | Type   | Description                                       |
|-----------|--------|---------------------------------------------------|
| token     | string | The Discord voice token to authenticate with      |
| endpoint  | string | The Discord voice endpoint to connect to          |
| sessionId | string | The Discord voice session id to authenticate with |

`token`, `endpoint`, and `sessionId` are the 3 required values for connecting to one of Discord's voice servers.
`sessionId` is provided by the Voice State Update event sent by Discord, whereas the `endpoint` and `token` are provided
with the Voice Server Update. Please refer to https://discord.com/developers/docs/topics/gateway-events#voice


#### Filters

Filters are used in above requests and look like this

| Field          | Type                                             | Description                                                                                  |
|----------------|--------------------------------------------------|----------------------------------------------------------------------------------------------|
| volume?        | float                                            | Adjusts the player volume from 0.0 to 5.0, where 1.0 is 100%. Values >1.0 may cause clipping |
| equalizer?     | array of [Equalizer](#equalizer) objects         | Adjusts 15 different bands                                                                   |
| karaoke?       | [Karaoke](#karaoke) object                       | Eliminates part of a band, usually targeting vocals                                          |
| timescale?     | [Timescale](#timescale) object                   | Changes the speed, pitch, and rate                                                           |
| tremolo?       | [Tremolo](#tremolo) object                       | Creates a shuddering effect, where the volume quickly oscillates                             |
| vibrato?       | [Vibrato](#vibrato) object                       | Creates a shuddering effect, where the pitch quickly oscillates                              |
| rotation?      | [Rotation](#rotation) object                     | Rotates the audio around the stereo channels/user headphones (aka Audio Panning)             |
| distortion?    | [Distortion](#distortion) object                 | Distorts the audio                                                                           |
| channelMix?    | [Channel Mix](#channel-mix) object               | Mixes both channels (left and right)                                                         |
| lowPass?       | [Low Pass](#low-pass) object                     | Filters higher frequencies                                                                   |
| pluginFilters? | map of [Plugin Filters](#plugin-filters) objects | Filter plugin configurations                                                                 |

##### Equalizer

There are 15 bands (0-14) that can be changed.
"gain" is the multiplier for the given band. The default value is 0. Valid values range from -0.25 to 1.0,
where -0.25 means the given band is completely muted, and 0.25 means it is doubled. Modifying the gain could also change the volume of the output.

<details markdown="1">
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

##### Plugin Filters

Plugins can add their own filters. The key is the name of the plugin, and the value is the configuration for that plugin. The configuration is plugin specific. See [Plugins](plugins.md) for more plugin information.

<details markdown="1">
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
  },
  "pluginFilters": {
    "myPlugin": {
      "myPluginKey": "myPluginValue"
    }
  }
}
```

</details>

---

### Get Players

Returns a list of players in this specific session.

```
GET /v4/sessions/{sessionId}/players
```

<details markdown="1">
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
      "pluginInfo": { ... },
      "userData": { ... }
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

### Get Player

Returns the player for this guild in this session.

```
GET /v4/sessions/{sessionId}/players/{guildId}
```

Response:

[Player](#Player) object

<details markdown="1">
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

### Update Player

Updates or creates the player for this guild if it doesn't already exist.

```
PATCH /v4/sessions/{sessionId}/players/{guildId}?noReplace=true
```

!!! info

    `sessionId` in the path should be the value from the [ready op](websocket.md#ready-op).

Query Params:

| Field      | Type | Description                                                                  |
|------------|------|------------------------------------------------------------------------------|
| noReplace? | bool | Whether to replace the current track with the new track. Defaults to `false` |

Request:

| Field              | Type                                        | Description                                                                                   |
|--------------------|---------------------------------------------|-----------------------------------------------------------------------------------------------|
| track?             | [Update Player Track](#update-player-track) | Specification for a new track to load, as well as user data to set                            |
| ~~encodedTrack?~~* | ?string                                     | The base64 encoded track to play. `null` stops the current track                              |
| ~~identifier?~~*   | string                                      | The identifier of the track to play                                                           |
| *position*?        | int                                         | The track position in milliseconds                                                            |
| endTime?           | ?int                                        | The track end time in milliseconds (must be > 0). `null` resets this if it was set previously |
| volume?            | int                                         | The player volume, in percentage, from 0 to 1000                                              |
| paused?            | bool                                        | Whether the player is paused                                                                  |
| filters?           | [Filters](#filters) object                  | The new filters to apply. This will override all previously applied filters                   |                   
| voice?             | [Voice State](#voice-state) object          | Information required for connecting to Discord                                                |

!!! info

    \* `encodedTrack` and `identifier` are mutually exclusive and deprecated. Use `track` instead.

#### Update Player Track

| Field        | Type    | Description                                                         |
|--------------|---------|---------------------------------------------------------------------|
| encoded?*    | ?string | The base64 encoded track to play. `null` stops the current track    |
| identifier?* | string  | The identifier of the track to play                                 |
| userData?    | object  | Additional track data to be sent back in the [Track Object](#track) |

!!! info

    \* `encoded` and `identifier` are mutually exclusive.

When `identifier` is used, Lavalink will try to resolve the identifier as a single track. An HTTP `400` error is returned when resolving a playlist, search result, or no tracks.

<details markdown="1">
<summary>Example Payload</summary>

```yaml
{
  "track": {
    "encoded": "...",
    "identifier": "...",       
    "userData": { ... }
  },
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

<details markdown="1">
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

### Destroy Player

Destroys the player for this guild in this session.

```
DELETE /v4/sessions/{sessionId}/players/{guildId}
```

Response:

204 - No Content

---

## Session API

### Update Session

Updates the session with the resuming state and timeout.

```
PATCH /v4/sessions/{sessionId}
```

Request:

| Field     | Type | Description                                         |
|-----------|------|-----------------------------------------------------|
| resuming? | bool | Whether resuming is enabled for this session or not |
| timeout?  | int  | The timeout in seconds (default is 60s)             |

<details markdown="1">
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

<details markdown="1">
<summary>Example Payload</summary>

```json
{
  "resuming": true,
  "timeout": 60
}
```

</details>

---

## Get Lavalink info

Request Lavalink information.

```
GET /v4/info
```

Response:

### Info Response

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

#### Version Object

Parsed [Semantic Versioning 2.0.0](https://semver.org/)

| Field      | Type    | Description                                                                        |
|------------|---------|------------------------------------------------------------------------------------|
| semver     | string  | The full version string of this Lavalink server                                    |
| major      | int     | The major version of this Lavalink server                                          |
| minor      | int     | The minor version of this Lavalink server                                          |
| patch      | int     | The patch version of this Lavalink server                                          |
| preRelease | ?string | The pre-release version according to semver as a `.` separated list of identifiers |
| build      | ?string | The build metadata according to semver as a `.` separated list of identifiers      |

#### Git Object

| Field      | Type   | Description                                                    |
|------------|--------|----------------------------------------------------------------|
| branch     | string | The branch this Lavalink server was built on                   |
| commit     | string | The commit this Lavalink server was built on                   |
| commitTime | int    | The millisecond unix timestamp for when the commit was created |

#### Plugin Object

| Field   | Type   | Description               |
|---------|--------|---------------------------|
| name    | string | The name of the plugin    |
| version | string | The version of the plugin |

<details markdown="1">
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

## Get Lavalink version

Request Lavalink version.

```
GET /version
```

Response:

```
4.0.0
```

---

## Get Lavalink stats

Request Lavalink statistics.

```
GET /v4/stats
```

Response:

`frameStats` is always missing for this endpoint.
[Stats](websocket.md#stats-object) object

<details markdown="1">
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

## RoutePlanner API

Additionally, there are a few REST endpoints for the ip rotation extension.


### Common Types ### {: #route-planner-api-types }

#### Route Planner Types

| Route Planner Type           | Description                                                                                                                 |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `RotatingIpRoutePlanner`     | IP address used is switched on ban. Recommended for IPv4 blocks or IPv6 blocks smaller than a /64.                          |
| `NanoIpRoutePlanner`         | IP address used is switched on clock update. Use with at least 1 /64 IPv6 block.                                            |
| `RotatingNanoIpRoutePlanner` | IP address used is switched on clock update, rotates to a different /64 block on ban. Use with at least 2x /64 IPv6 blocks. |
| `BalancingIpRoutePlanner`    | IP address used is selected at random per request. Recommended for larger IP blocks.                                        |

#### Details Object

| Field               | Type                                                  | Description                                                                           | Valid Types                                        |
|---------------------|-------------------------------------------------------|---------------------------------------------------------------------------------------|----------------------------------------------------|
| ipBlock             | [IP Block](#ip-block-object) object                   | The ip block being used                                                               | all                                                |
| failingAddresses    | array of [Failing Addresses](#failing-address-object) | The failing addresses                                                                 | all                                                |
| rotateIndex         | string                                                | The number of rotations                                                               | `RotatingIpRoutePlanner`                           |
| ipIndex             | string                                                | The current offset in the block                                                       | `RotatingIpRoutePlanner`                           |
| currentAddress      | string                                                | The current address being used                                                        | `RotatingIpRoutePlanner`                           |
| currentAddressIndex | string                                                | The current offset in the ip block                                                    | `NanoIpRoutePlanner`, `RotatingNanoIpRoutePlanner` |
| blockIndex          | string                                                | The information in which /64 block ips are chosen. This number increases on each ban. | `RotatingNanoIpRoutePlanner`                       |

#### IP Block Object

| Field | Type                            | Description              |
|-------|---------------------------------|--------------------------|
| type  | [IP Block Type](#ip-block-type) | The type of the ip block |
| size  | string                          | The size of the ip block |

#### IP Block Type

| IP Block Type  | Description         |
|----------------|---------------------|
| `Inet4Address` | The ipv4 block type |
| `Inet6Address` | The ipv6 block type |

#### Failing Address Object

| Field            | Type   | Description                                              |
|------------------|--------|----------------------------------------------------------|
| failingAddress   | string | The failing address                                      |
| failingTimestamp | int    | The timestamp when the address failed                    |
| failingTime      | string | The timestamp when the address failed as a pretty string |

---

### Get RoutePlanner status

```
GET /v4/routeplanner/status
```

Response:

| Field   | Type                                        | Description                                                           |
|---------|---------------------------------------------|-----------------------------------------------------------------------|
| class   | ?[Route Planner Type](#route-planner-types) | The name of the RoutePlanner implementation being used by this server |
| details | ?[Details](#details-object) object          | The status details of the RoutePlanner                                |

<details markdown="1">
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

### Unmark a failed address

```
POST /v4/routeplanner/free/address
```

Request:

| Field   | Type   | Description                                                                 |
|---------|--------|-----------------------------------------------------------------------------|
| address | string | The address to unmark as failed. This address must be in the same ip block. |

<details markdown="1">
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

### Unmark all failed address

```
POST /v4/routeplanner/free/all
```

Response:

204 - No Content
