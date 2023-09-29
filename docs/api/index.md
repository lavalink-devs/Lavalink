---
description: Lavalink API Implementation guidelines
---

# API Implementation guidelines

How to write your own client.

## Required capabilities of your Discord library

* You must be able to send messages via a shard's gateway connection.
* You must be able to intercept voice server & voice state updates from the gateway on your shard connection.

## Insomnia Collection

You can find an [Insomnia](https://insomnia.rest/) collection in the [here](Insomnia.json) which contains all the endpoints and their respective payloads.

## Significant changes

### v3.7.0 -> v4.0.0

* removed all non version `/v3` or `/v4` endpoints (except `/version`).
* `/v4/websocket` does not accept any messages anymore.
* `v4` uses the `sessionId` instead of the `resumeKey` for resuming.
* `v4` now returns the tracks `artworkUrl` and `isrc` if the source supports it.
* removal of deprecated json fields like `track`.
* addition of `artworkUrl` and `isrc` fields to the [Track Info](#track-info) object.
* addition of the full [Track](#track) object in [TrackStartEvent](#trackstartevent), [TrackEndEvent](#trackendevent), [TrackExceptionEvent](#trackexceptionevent) and [TrackStuckEvent](#trackstuckevent).
* updated capitalization of [Track End Reason](#track-end-reason) and [Severity](#severity)
* reworked [Load Result](#track-loading-result) object

<details markdown="1">
<summary>v4.0.0 Migration Guide</summary>

All websocket ops are removed as of `v4.0.0` and replaced with the following endpoints and json fields:

* `play` -> [Update Player Endpoint](#update-player) `encodedTrack` or `identifier` field
* `stop` -> [Update Player Endpoint](#update-player) `encodedTrack` field with `null`
* `pause` -> [Update Player Endpoint](#update-player) `pause` field
* `seek` -> [Update Player Endpoint](#update-player) `position` field
* `volume` -> [Update Player Endpoint](#update-player) `volume` field
* `filters` -> [Update Player Endpoint](#update-player) `filters` field
* `destroy` -> [Destroy Player Endpoint](#destroy-player)
* `voiceUpdate` -> [Update Player Endpoint](#update-player) `voice` field
* `configureResuming` -> [Update Session Endpoint](#update-session)

</details>

<details markdown="1">
<summary>Older versions</summary>

### v3.6.0 -> v3.7.0

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

<details markdown="1">
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

### v3.3 -> v3.4

* Added filters
* The `error` string on the `TrackExceptionEvent` has been deprecated and replaced by
  the [Exception](#exception-object) object following the same structure as the `LOAD_FAILED` error on [`/loadtracks`](#track-loading).
* Added the `connected` boolean to player updates.
* Added source name to REST api track objects
* Clients are now requested to make their name known during handshake

### v2.0 -> v3.0

* The response of `/loadtracks` has been completely changed (again since the initial v3.0 pre-release).
* Lavalink v3.0 now reports its version as a handshake response header.
  `Lavalink-Major-Version` has a value of `3` for v3.0 only. It's missing for any older version.

### v1.3 -> v2.0

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

## Resuming

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

## Common pitfalls

Admittedly Lavalink isn't inherently the most intuitive thing ever, and people tend to run into the same mistakes over again. Please double-check the following if you run into problems developing your client, and you can't connect to a voice channel or play audio:

1. Check that you are intercepting `VOICE_SERVER_UPDATE`s and `VOICE_STATE_UPDATE`s to **Lavalink**. You only need the `endpoint`, `token`, and `session_id`.
2. Check that you aren't expecting to hear audio when you have forgotten to queue something up OR forgotten to join a voice channel.
3. Check that you are not trying to create a voice connection with your Discord library.
4. When in doubt, check the debug logfile at `/logs/debug.log`.
