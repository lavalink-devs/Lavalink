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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                LavalinkPlugin.Async async = clazz.getAnnotation(LavalinkPlugin.Async.class);
                LavalinkPlugin plugin = ctor.newInstance();
                if(async != null) {
                    plugin = new AsyncPlugin(plugin, async);
                }
                plugins.add(plugin);
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

    private static class AsyncPlugin implements LavalinkPlugin {
        private final LavalinkPlugin actualPlugin;
        private final ExecutorService executor;

        private AsyncPlugin(LavalinkPlugin actualPlugin, Async async) {
            this.actualPlugin = actualPlugin;
            String str = actualPlugin.toString();
            this.executor = Executors.newFixedThreadPool(async.corePoolSize(), r->{
                Thread t = new Thread(r, "PluginThread-" + str);
                t.setDaemon(true);
                return t;
            });
        }

        @Override
        public void onStart(SocketServer server) {
            executor.execute(()->{
                try {
                    actualPlugin.onStart(server);
                } catch(Exception e) {
                    LOGGER.error("Error calling onStart() for {}", actualPlugin, e);
                }
            });
        }

        @Override
        public void onShutdown() {
            executor.execute(()->{
                try {
                    actualPlugin.onShutdown();
                } catch(Exception e) {
                    LOGGER.error("Error calling onShutdown() for {}", actualPlugin, e);
                }
                executor.shutdown();
            });
        }

        @Override
        public int hashCode() {
            return actualPlugin.hashCode();
        }

        @Override
        public String toString() {
            return actualPlugin.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof AsyncPlugin) {
                return ((AsyncPlugin) obj).actualPlugin.equals(actualPlugin);
            }
            return obj instanceof LavalinkPlugin && actualPlugin.equals(obj);
        }
    }
}
