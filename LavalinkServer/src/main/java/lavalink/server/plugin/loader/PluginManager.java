package lavalink.server.plugin.loader;

import lavalink.plugin.ISocketContext;
import lavalink.plugin.ISocketServer;
import lavalink.plugin.LavalinkPlugin;
import lavalink.server.config.PluginConfig;
import lavalink.server.config.PluginInfo;
import lavalink.server.io.SocketContext;
import lavalink.server.io.SocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    private final PluginLoader loader;
    private final List<LavalinkPlugin> plugins = new ArrayList<>();
    private final AutowireCapableBeanFactory factory;

    public PluginManager(ClassLoader loader, AutowireCapableBeanFactory factory) {
        this.loader = new PluginLoader(loader);
        this.factory = factory;
    }

    public PluginManager(AutowireCapableBeanFactory factory) {
        this(PluginManager.class.getClassLoader(), factory);
    }

    public void loadFrom(PluginConfig config) {
        for(Map.Entry<String, PluginInfo> plugin : config.getLocations().entrySet()) {
            String name = plugin.getKey();
            PluginInfo info = plugin.getValue();
            if(!info.isEnabled()) {
                LOGGER.info("Plugin {} is disabled, skipping...", name);
                continue;
            }
            LOGGER.info("Loading plugin {} from {}", name, info.getPath());
            try {
                load(info.getPath());
            } catch(PluginLoadException e) {
                LOGGER.error("Error loading plugin {}", name, e);
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

    public synchronized void callOnWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) {
        for(LavalinkPlugin plugin : plugins) {
            try {
                plugin.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
            } catch(Exception e) {
                LOGGER.error("Error calling onWebsocketHandshakeReceivedAsServer() for {}", plugin, e);
            }
        }
    }

    public synchronized void callOnOpen(WebSocket webSocket, ClientHandshake clientHandshake, SocketContext context) {
        for(LavalinkPlugin plugin : plugins) {
            try {
                plugin.onOpen(webSocket, clientHandshake, context);
            } catch(Exception e) {
                LOGGER.error("Error calling onOpen() for {}", plugin, e);
            }
        }
    }

    public synchronized void callOnClose(WebSocket webSocket, int code, String reason, SocketContext context) {
        for(LavalinkPlugin plugin : plugins) {
            try {
                plugin.onClose(webSocket, code, reason, context);
            } catch(Exception e) {
                LOGGER.error("Error calling onClose() for {}", plugin, e);
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
                LavalinkPlugin.Async async = clazz.getAnnotation(LavalinkPlugin.Async.class);
                LavalinkPlugin plugin = factory.createBean(clazz);
                if(async != null) {
                    plugin = new AsyncPlugin(plugin, async);
                }
                plugins.add(plugin);
            } catch(Exception e) {
                LOGGER.error("Unable to create plugin {}", clazz, e);
            }
        }
    }

    private static class AsyncPlugin implements LavalinkPlugin {
        private final LavalinkPlugin actualPlugin;
        private final ExecutorService executor;

        private AsyncPlugin(LavalinkPlugin actualPlugin, Async async) {
            this.actualPlugin = actualPlugin;
            String str = actualPlugin.toString();
            this.executor = Executors.newFixedThreadPool(Math.max(1, async.corePoolSize()), r->{
                Thread t = new Thread(r, "PluginThread-" + str);
                t.setDaemon(true);
                return t;
            });
        }

        @Override
        public void onStart(ISocketServer server) {
            executor.execute(()->{
                try {
                    actualPlugin.onStart(server);
                } catch(Exception e) {
                    LOGGER.error("Error calling onStart() for {}", actualPlugin, e);
                }
            });
        }

        @Override
        public void onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) {
            executor.execute(()->{
                try {
                    actualPlugin.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
                } catch(Exception e) {
                    LOGGER.error("Error calling onWebsocketHandshakeReceivedAsServer() for {}", actualPlugin, e);
                }
            });
        }

        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake, ISocketContext context) {
            executor.execute(()->{
                try {
                    actualPlugin.onOpen(webSocket, clientHandshake, context);
                } catch(Exception e) {
                    LOGGER.error("Error calling onOpen() for {}", actualPlugin, e);
                }
            });
        }

        @Override
        public void onClose(WebSocket webSocket, int code, String reason, ISocketContext context) {
            executor.execute(()->{
                try {
                    actualPlugin.onClose(webSocket, code, reason, context);
                } catch(Exception e) {
                    LOGGER.error("Error calling onClose() for {}", actualPlugin, e);
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
