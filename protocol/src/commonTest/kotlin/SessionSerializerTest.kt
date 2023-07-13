import dev.arbjerg.lavalink.protocol.v4.Omissible
import dev.arbjerg.lavalink.protocol.v4.Session
import dev.arbjerg.lavalink.protocol.v4.SessionUpdate
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.minutes

class SessionSerializerTest {
    @Test
    @JsName("test1")
    fun `test session can be serialized`() {
        //language=json
        val json = """{
          "resuming": true,
          "timeout": 60
        }
        """

        test<Session>(json) {
            resuming shouldBe true
            timeout shouldBe 1.minutes
        }
    }

    @Test
    @JsName("test2")
    fun `test session update can be serialized`() {
        //language=json
        val json = """{
          "resuming": true,
          "timeout": 60
        }
        """

        test<SessionUpdate>(json) {
            resuming shouldBe true
            timeout shouldBe 1.minutes
        }
    }
    @Test
    @JsName("test3")
    fun `test session update can be serialized when missing`() {
        //language=json
        val json = """{}"""

        test<SessionUpdate>(json) {
            assertIs<Omissible.Omitted<*>>(resuming)
            assertIs<Omissible.Omitted<*>>(timeout)
        }
    }
}
