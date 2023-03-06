import dev.arbjerg.lavalink.protocol.v4.Omissible
import dev.arbjerg.lavalink.protocol.v4.Player
import dev.arbjerg.lavalink.protocol.v4.PlayerUpdate
import dev.arbjerg.lavalink.protocol.v4.VoiceState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

//language=json
private const val json = """
[
  {
    "guildId": "123",
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
        "sourceName": "youtube",
        "artworkUrl": null,
        "isrc": null
      },
      "pluginInfo": {}
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
    "state": {
        "time":  1,
        "position": 1,
        "connected":  true,
        "ping": 10
    },
    "filters": $filters
  }
]
"""

//language=json
const val updateJson = """
{
  "identifier": "...",
  "endTime": 0,
  "volume": 100,
  "position": 32400,
  "paused": false,
  "voice": {
    "token": "...",
    "endpoint": "...",
    "sessionId": "..."
  }
}
"""

class PlayerSerializerTest {
    @Test
    @JsName("test1")
    fun `test player serialization`() {
        test<List<Player>>(json) {
            onEach {
                guildId shouldBe 123UL
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
                        sourceName shouldBe "youtube"
                        artworkUrl shouldBe null
                        isrc shouldBe null
                    }
                }
                volume shouldBe 100
                paused shouldBe false
                voice {
                    token shouldBe "..."
                    endpoint shouldBe "..."
                    sessionId shouldBe "..."
                    connected shouldBe true
                    ping shouldBe 10
                }
                state {
                    time shouldBe 1
                    position shouldBe 1
                    connected shouldBe true
                    ping shouldBe 10
                }
            }
        }
    }

    @Test
    @JsName("test2")
    fun `test empty update serialization`() {
        //language=json
        val json = """{}"""

        test<PlayerUpdate>(json) {
            assertIs<Omissible.Omitted<*>>(encodedTrack)
            assertIs<Omissible.Omitted<*>>(identifier)
            assertIs<Omissible.Omitted<*>>(position)
            assertIs<Omissible.Omitted<*>>(endTime)
            assertIs<Omissible.Omitted<*>>(volume)
            assertIs<Omissible.Omitted<*>>(paused)
            assertIs<Omissible.Omitted<*>>(filters)
            assertIs<Omissible.Omitted<*>>(voice)
        }
    }

    @Test
    @JsName("test3")
    fun `test encodedTrack and identifier exclusivity`() {
        //language=json
        val json = """{"encodedTrack":  "", "identifier":  ""}"""

        assertFailsWith<IllegalArgumentException> { Json.decodeFromString(json) }
    }

    @Test
    @JsName("test4")
    fun `test update player serialization`() {
        test<PlayerUpdate>(updateJson) {
            identifier shouldBe "..."
            endTime shouldBe 0
            volume shouldBe 100
            position shouldBe 32400
            paused shouldBe false
            assertIs<Omissible.Present<*>>(voice)
            (voice as Omissible.Present<VoiceState>).value {
                token shouldBe "..."
                endpoint shouldBe "..."
                sessionId shouldBe "..."
            }
        }
    }
}
