import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.Message
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs

class MessageSerializerTest {
    @Test
    @JsName("test1")
    fun `test ready event serializer`() {
        //language=json
        val json = """
            {
              "op": "ready",
              "resumed": false,
              "sessionId": "..."
            }
        """.trimIndent()
        test<Message>(json) {
            assertIs<Message.ReadyEvent>(this)
            op shouldBe Message.Op.Ready
            resumed shouldBe false
            sessionId shouldBe "..."
        }
    }

    @Test
    @JsName("test2")
    fun `test player update event serializer`() {
        //language=json
        val json = """
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
        """.trimIndent()
        test<Message>(json) {
            assertIs<Message.PlayerUpdateEvent>(this)
            op shouldBe Message.Op.PlayerUpdate
            guildId shouldBe "..."
            state {
                time shouldBe 1500467109
                position shouldBe 60000
                connected shouldBe true
                ping shouldBe 50
            }
        }
    }

    @Test
    @JsName("test3")
    fun `test stats event serializer`() {
        //language=json
        val json = """
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
        """.trimIndent()
        test<Message>(json) {
            assertIs<Message.StatsEvent>(this)
            op shouldBe Message.Op.Stats
            players shouldBe 1
            playingPlayers shouldBe 1
            uptime shouldBe 123456789
            memory {
                free shouldBe 123456789
                used shouldBe 123456789
                allocated shouldBe 123456789
                reservable shouldBe 123456789
            }
            cpu {
                cores shouldBe 4
                systemLoad shouldBe 0.5
                lavalinkLoad shouldBe 0.5
            }
            frameStats {
                sent shouldBe 123456789
                nulled shouldBe 123456789
                deficit shouldBe 123456789
            }
        }
    }

    @Test
    @JsName("test4")
    fun `test track start event serializer`() {
        //language=json
        val json = """
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
                "pluginInfo": {},
                "userData": {}
              }
            }
        """.trimIndent()

        test<Message>(json) {
            assertIs<Message.EmittedEvent.TrackStartEvent>(this)
            op shouldBe Message.Op.Event
            type shouldBe Message.EmittedEvent.Type.TrackStart
            guildId shouldBe "..."
            track {
                encoded shouldBe "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
                info {
                    identifier shouldBe "dQw4w9WgXcQ"
                    isSeekable shouldBe true
                    author shouldBe "RickAstleyVEVO"
                    length shouldBe 212000
                    isStream shouldBe false
                    position shouldBe 0
                    title shouldBe "Rick Astley - Never Gonna Give You Up"
                    uri shouldBe "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    artworkUrl shouldBe "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                    isrc shouldBe null
                    sourceName shouldBe "youtube"
                }
            }
        }
    }

    @Test
    @JsName("test5")
    fun `test track end event serializer`() {
        //language=json
        val json = """
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
                "pluginInfo": {},
                "userData": {}
              },
              "reason": "finished"
            }
        """.trimIndent()

        test<Message>(json) {
            assertIs<Message.EmittedEvent.TrackEndEvent>(this)
            op shouldBe Message.Op.Event
            type shouldBe Message.EmittedEvent.Type.TrackEnd
            guildId shouldBe "..."
            track {
                encoded shouldBe "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
                info {
                    identifier shouldBe "dQw4w9WgXcQ"
                    isSeekable shouldBe true
                    author shouldBe "RickAstleyVEVO"
                    length shouldBe 212000
                    isStream shouldBe false
                    position shouldBe 0
                    title shouldBe "Rick Astley - Never Gonna Give You Up"
                    uri shouldBe "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    artworkUrl shouldBe "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                    isrc shouldBe null
                    sourceName shouldBe "youtube"
                }
                reason shouldBe Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.FINISHED
            }
        }
    }

    @Test
    @JsName("test7")
    fun `test track exception event serializer`() {
        //language=json
        val json = """
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
                "pluginInfo": {},
                "userData": {}
              },
              "exception": {
                "message": "...",
                "severity": "common",
                "cause": "...",
                "causeStackTrace": "..."
              }
            }
        """.trimIndent()

        test<Message>(json) {
            assertIs<Message.EmittedEvent.TrackExceptionEvent>(this)
            op shouldBe Message.Op.Event
            type shouldBe Message.EmittedEvent.Type.TrackException
            guildId shouldBe "..."
            track {
                encoded shouldBe "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
                info {
                    identifier shouldBe "dQw4w9WgXcQ"
                    isSeekable shouldBe true
                    author shouldBe "RickAstleyVEVO"
                    length shouldBe 212000
                    isStream shouldBe false
                    position shouldBe 0
                    title shouldBe "Rick Astley - Never Gonna Give You Up"
                    uri shouldBe "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    artworkUrl shouldBe "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                    isrc shouldBe null
                    sourceName shouldBe "youtube"
                }
                exception {
                    message shouldBe "..."
                    severity shouldBe Exception.Severity.COMMON
                    cause shouldBe "..."
                    causeStackTrace shouldBe "..."
                }
            }
        }
    }

    @Test
    @JsName("test8")
    fun `test track stuck event serializer`() {
        //language=json
        val json = """
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
                "pluginInfo": {},
                "userData": {}
              },
              "thresholdMs": 123456789
            }
        """.trimIndent()

        test<Message>(json) {
            assertIs<Message.EmittedEvent.TrackStuckEvent>(this)
            op shouldBe Message.Op.Event
            type shouldBe Message.EmittedEvent.Type.TrackStuck
            guildId shouldBe "..."
            track {
                encoded shouldBe "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
                info {
                    identifier shouldBe "dQw4w9WgXcQ"
                    isSeekable shouldBe true
                    author shouldBe "RickAstleyVEVO"
                    length shouldBe 212000
                    isStream shouldBe false
                    position shouldBe 0
                    title shouldBe "Rick Astley - Never Gonna Give You Up"
                    uri shouldBe "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    artworkUrl shouldBe "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                    isrc shouldBe null
                    sourceName shouldBe "youtube"
                }
                thresholdMs shouldBe 123456789
            }
        }
    }

    @Test
    @JsName("test9")
    fun `test websocket closed event serializer`() {
        //language=json
        val json = """
            {
              "op": "event",
              "type": "WebSocketClosedEvent",
              "guildId": "...",
              "code": 4006,
              "reason": "Your session is no longer valid.",
              "byRemote": true
            }
        """.trimIndent()

        test<Message>(json) {
            assertIs<Message.EmittedEvent.WebSocketClosedEvent>(this)
            op shouldBe Message.Op.Event
            type shouldBe Message.EmittedEvent.Type.WebSocketClosed
            guildId shouldBe "..."
            code shouldBe 4006
            reason shouldBe "Your session is no longer valid."
            byRemote shouldBe true
        }
    }
}
