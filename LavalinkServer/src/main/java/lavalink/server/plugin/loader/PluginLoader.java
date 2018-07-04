package lavalink.server.plugin.loader;

import lavalink.plugin.LavalinkPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private final ClassLoader parentLoader;

    public PluginLoader(ClassLoader loader) {
        this.parentLoader = loader;
    }

    public List<Class<? extends LavalinkPlugin>> load(File from) throws PluginLoadException {
        try {
            if(from.isDirectory()) {
                return new DirectoryPluginFinder(parentLoader, from).find();
            }
            if(from.isFile() && from.getName().endsWith(".jar")) {
                return new JarPluginFinder(parentLoader, from).find();
            }
        } catch(IOException e) {
            throw new PluginLoadException(e);
        }
        throw new PluginLoadException("Unable to load plugins from " + from + ": no suitable loader found");
    }

    //has annotation but doesn't implement the interface
    static void logBadPlugin(String name) {
        LOGGER.error("Plugin class {} has LavalinkPlugin.AutoRegister annotation but does not implement LavalinkPlugin", name);
    }
}
