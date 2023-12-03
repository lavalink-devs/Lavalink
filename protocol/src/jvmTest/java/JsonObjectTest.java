import dev.arbjerg.lavalink.protocol.v4.utils.JsonObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonObjectTest {
    @Test
    public void testBuilder() {
        var obj = JsonObjects.builder()
                .put("time", 42)
                .putArray("children")
                .add("banana_bread")
                .build()
                .putNull("is_null")
                .build()
                .toString();

        var source = "{\"time\":42,\"children\":[\"banana_bread\"],\"is_null\":null}";

        Assertions.assertEquals(source, obj);
    }

    @Test
    public void testNull() {
        Assertions.assertEquals("{}", JsonObjects.empty().toString());
    }
}
