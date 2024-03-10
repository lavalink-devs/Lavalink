---
description: Lavalink API Implementation guidelines
---

# API Implementation guidelines

## Required capabilities of your Discord library

* You must be able to send messages via a shard's gateway connection.
* You must be able to intercept voice server & voice state updates from the gateway on your shard connection.

## Protocol

### Reference

Fields marked with `?` are optional and types marked with `?` are nullable.

## Resuming

What happens after your client disconnects is dependent on whether the session has been configured for resuming.

* If resuming is disabled all voice connections are closed immediately.
* If resuming is enabled all music will continue playing. You will then be able to resume your session, allowing you to control the players again.

To enable resuming, you must call the [Update Session](../api/rest.md#update-session) endpoint with the `resuming` and `timeout`.

To resume a session, specify the session id in your WebSocket handshake request headers:

```
Session-Id: The id of the session you want to resume.
```

You can tell if your session was resumed by looking at the handshake response header `Session-Resumed` which is either `true` or `false`.

```
Session-Resumed: true
```

In case your websocket library doesn't support reading headers you can listen for the [ready op](../api/websocket.md#ready-op) and check the `resumed` field.

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
