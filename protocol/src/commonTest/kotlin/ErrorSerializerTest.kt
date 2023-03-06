import dev.arbjerg.lavalink.protocol.v4.Error
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlin.js.JsName
import kotlin.test.Test

//language=json
const val exampleError = """
{
  "timestamp": 1667857581613,
  "status": 404,
  "error": "Not Found",
  "trace": "...",
  "message": "Session not found",
  "path": "/v4/sessions/xtaug914v9k5032f/players/817327181659111454"
}
"""

class ErrorSerializerTest {
    @Test
    @JsName("test1")
    fun `test REST error serialization`() {
        test<Error>(exampleError) {
            timestamp shouldBe Instant.fromEpochMilliseconds(1667857581613)
            status shouldBe HttpStatusCode.NotFound
            error shouldBe "Not Found"
            trace shouldBe "..."
            message shouldBe "Session not found"
            path shouldBe "/v4/sessions/xtaug914v9k5032f/players/817327181659111454"
        }
    }
}
