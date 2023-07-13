import dev.arbjerg.lavalink.protocol.v4.Omissible
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OmissibleTest {
    @Test
    @JsName("test1")
    fun `test value can be serialized`() {
        //language=json
        val json = """{"value": 1}"""

        @Serializable
        data class Class(val value: Omissible<Int>)

        test<Class>(json) {
            value shouldBe 1
        }
    }

    @Test
    @JsName("test2")
    fun `null value can be serialized`() {
        //language=json
        val json = """{"value": null}"""

        @Serializable
        data class Class(val value: Omissible<Int?>)

        test<Class>(json) {
            value shouldBe null
        }
    }

    @Test
    @JsName("test3")
    fun `null throws error if not allowed`() {
        //language=json
        val json = """{"value": null}"""

        @Serializable
        data class Class(val value: Omissible<Int>)

        val exception = assertFailsWith<SerializationException> { Json.decodeFromString<Class>(json) }
        exception {
            message shouldBe "descriptor for kotlin.Int was not nullable but null mark was encountered"
        }
    }

    @Test
    @JsName("test4")
    fun `missing can be deserialized`() {
        //language=json
        val json = """{}"""

        @Serializable
        data class Class(val value: Omissible<Int> = Omissible.Omitted())

        test<Class>(json) {
            assertIs<Omissible.Omitted<*>>(value)
        }
    }

    // this tests only works on the default json configuration of Lavalink
    @Test
    @JsName("test5")
    fun `missing can be serialized`() {
        //language=json
        val json = """{}"""

        @Serializable
        data class Class(val value: Omissible<Int> = Omissible.Omitted())

        val encoded = dev.arbjerg.lavalink.protocol.v4.json.encodeToString(Class())
        assertEquals(json, encoded)
    }
}
