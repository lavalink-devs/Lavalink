package lavalink.server.plugin;

import lavalink.server.io.SocketServer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface LavalinkPlugin {
    //with the current ASM based implementation CLASS would be enough, but if it's ever changed to a reflection
    //based implementation in the future the annotation will still be kept
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AutoRegister {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Async {
        int corePoolSize() default 1;
    }

    default void onStart(SocketServer server) {}

    default void onShutdown() {}
}
