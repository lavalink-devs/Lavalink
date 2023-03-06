import dev.arbjerg.lavalink.protocol.v4.Omissible
import dev.arbjerg.lavalink.protocol.v4.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.js.JsName
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

inline fun <reified T : Any> test(input: String, validator: T.() -> Unit) {
    val decoded = json.decodeFromString<T>(input)
    validator(decoded)
    val serialized = json.encodeToString(decoded)
    val decoded2 = json.decodeFromString<T>(serialized)
    assertEquals(decoded, decoded2)
}

infix fun <T> T.shouldBe(expected: T) = assertEquals(expected, this)
infix fun <T> Omissible<T>.shouldBe(expected: T) {
    assertIs<Omissible.Present<*>>(this)
    assertEquals(expected, value)
}

operator fun <T> T?.invoke(block: T.() -> Unit) {
    assertNotNull(this)
    block(this)
}

fun <T> List<T>.onEach(action: T.() -> Unit) = forEach(action)
