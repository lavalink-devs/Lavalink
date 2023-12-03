import dev.arbjerg.lavalink.protocol.v4.utils.JsonPrimitives;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonPrimitiveTest {

    @Test
    public void testNull() {
        Assertions.assertEquals("null", JsonPrimitives.jsonNull().toString());
    }

    @Test
    public void testBoolean() {
        Assertions.assertEquals("true", JsonPrimitives.from(true).toString());
    }

    @Test
    public void testNumber() {
        testNumber(Byte.MAX_VALUE);
        testNumber(Short.MAX_VALUE);
        testNumber(Integer.MAX_VALUE);
        testNumber(Long.MAX_VALUE);
        testNumber(Float.MAX_VALUE);
        testNumber(Double.MAX_VALUE);
    }

    @Test
    public void testString() {
        Assertions.assertEquals("\"test123\"", JsonPrimitives.from("test123").toString());
    }

    private void testNumber(Number number) {
        Assertions.assertEquals(String.valueOf(number), JsonPrimitives.from(number).toString());
    }
}
