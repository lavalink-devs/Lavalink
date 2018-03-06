package lavalink.client;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by napster on 06.03.18.
 */
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RequireSystemPropertyExists.class)
public @interface RequireSystemProperty {

    /**
     * @return an array of System property keys
     */
    String[] value();
}
