package lavalink.server.config

import lavalink.server.bootstrap.PluginManager
import lavalink.server.bootstrap.PluginsConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestConfig {

    @Bean
    fun pluginManager() = PluginManager(PluginsConfig())

}