import dev.arbjerg.lavalink.protocol.v4.StatsData
import kotlin.js.JsName
import kotlin.test.Test

//language=json
const val test = """
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
"""

class StatsTest {
    @Test
    @JsName("test1")
    fun `test stats can be serialized`() {
        test<StatsData>(test) {
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
        }
    }
}
