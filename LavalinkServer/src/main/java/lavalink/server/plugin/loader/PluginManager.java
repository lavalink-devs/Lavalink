package lavalink.server.plugin.loader;

import lavalink.server.config.PluginConfig;
import lavalink.server.io.SocketServer;
import lavalink.server.plugin.LavalinkPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    private final PluginLoader loader;
    private final List<LavalinkPlugin> plugins = new ArrayList<>();

    public PluginManager(ClassLoader loader) {
        this.loader = new PluginLoader(loader);
    }

    public PluginManager() {
        this(PluginManager.class.getClassLoader());
    }

    public void loadFrom(PluginConfig config) {
        for(String s : config.getPaths()) {
            LOGGER.info("Loading plugins from {}", s);
            try {
                load(s);
            } catch(PluginLoadException e) {
                LOGGER.error("Error loading plugin from {}", s, e);
            }
        }
    }

    public boolean hasPlugins() {
        return plugins.size() > 0;
    }

    public synchronized void callOnStart(SocketServer server) {
        for(LavalinkPlugin plugin : plugins) {
            try {
                plugin.onStart(server);
            } catch(Exception e) {
                LOGGER.error("Error calling onStart() for {}", plugin, e);
            }
        }
    }

    public synchronized void callOnShutdown() {
        for(LavalinkPlugin plugin : plugins) {
            try {
                plugin.onShutdown();
            } catch(Exception e) {
                LOGGER.error("Error calling onShutdown() for {}", plugin, e);
            }
        }
    }

    public synchronized void load(String path) throws PluginLoadException {
        List<Class<? extends LavalinkPlugin>> classes = loader.load(new File(path));
        for(Class<? extends LavalinkPlugin> clazz : classes) {
            try {
                Constructor<? extends LavalinkPlugin> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                plugins.add(ctor.newInstance());
            } catch(NoSuchMethodException e) {
                LOGGER.error("Plugin {} does not have a zero arg constructor", clazz);
            } catch(InstantiationException e) {
                LOGGER.error("Error instantiating {}", clazz, e);
            } catch(IllegalAccessException e) {
                LOGGER.error("Unable to access constructor for {}", clazz);
            } catch(InvocationTargetException e) {
                LOGGER.error("Unable to instantiate {}", clazz, e);
            }
        }
    }
}
