import dev.arbjerg.lavalink.protocol.v4.utils.JsonArrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonArrayTest {
    @Test
    public void testEmptyArray() {
        Assertions.assertEquals("[]", JsonArrays.empty().toString());
    }

    @Test
    public void testBuilder() {
        var array = JsonArrays.builder()
                .add("21")
                .addNull()
                .addObject()
                    .put("test", 21)
                    .build()
                .build()
                .toString();

        var source = "[\"21\",null,{\"test\":21}]";

        Assertions.assertEquals(source, array);
    }
}
