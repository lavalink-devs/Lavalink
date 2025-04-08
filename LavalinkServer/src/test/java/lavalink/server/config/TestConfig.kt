package lavalink.server.config

import lavalink.server.bootstrap.PluginLoader
import lavalink.server.bootstrap.PluginSystemImpl
import lavalink.server.bootstrap.PluginsConfig
import lavalink.server.info.AppInfo
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {

    @Bean
    fun pluginManager() = PluginSystemImpl(PluginsConfig(), PluginLoader(PluginsConfig(), AppInfo()))

}