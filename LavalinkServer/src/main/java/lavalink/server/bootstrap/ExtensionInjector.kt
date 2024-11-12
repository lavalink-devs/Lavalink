package lavalink.server.bootstrap

import lavalink.server.config.RequestHandlerMapping
import org.pf4j.ExtensionWrapper
import org.pf4j.spring.ExtensionsInjector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.context.annotation.AnnotationConfigRegistry
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.full.hasAnnotation

private val log = LoggerFactory.getLogger(LavalinkExtensionInjector::class.java)

class LavalinkExtensionInjector(pluginManager: PluginLoader, factory: AbstractAutowireCapableBeanFactory) :
    ExtensionsInjector(pluginManager, factory) {

    // We override this to use ExtensionWrappers instead, because the original does not respect ordinals
    override fun injectExtensions() {
        val pluginLoader = springPluginManager as PluginLoader
        // add extensions from classpath (non plugin)
        val internalExtensions =
            springPluginManager.plugins.flatMap { pluginLoader.extensionFinder.find(null) }

        val pluginExtensions = springPluginManager.startedPlugins.flatMap { plugin ->
            log.debug("Registering extensions of the plugin '{}' as beans", plugin.pluginId)

            pluginLoader.extensionFinder.find(plugin.pluginId)
        }
        (internalExtensions + pluginExtensions)
            .sortedBy { it.ordinal }
            .register()
    }

    fun List<ExtensionWrapper<*>>.register() = forEach { extensionWrapper ->
        log.debug("Register extension '{}' as bean", extensionWrapper.descriptor.extensionClass.name)
        try {
            registerExtension(extensionWrapper.descriptor.extensionClass)
        } catch (e: ClassNotFoundException) {
            log.error(e.message, e)
        }
    }

    override fun registerExtension(extensionClass: Class<*>) {
        val extensionBeanMap = springPluginManager.applicationContext.getBeansOfType(extensionClass)
        if (extensionBeanMap.isEmpty()) {
            val extension = springPluginManager.getExtensionFactory().create(extensionClass)

            if (extensionClass.kotlin.hasAnnotation<Configuration>()) {
                (springPluginManager.applicationContext as AnnotationConfigRegistry).register(extensionClass)
            }

            if (extensionClass.kotlin.hasAnnotation<ConfigurationProperties>()) {
                val configBinder =
                    springPluginManager.applicationContext.getBean<ConfigurationPropertiesBindingPostProcessor>()
                configBinder.postProcessBeforeInitialization(extension, extensionClass.getName())
            }

            this.beanFactory.registerSingleton(extensionClass.getName(), extension)
            this.beanFactory.autowireBean(extension)

            if (extension::class.hasAnnotation<RestController>()) {
                log.debug(
                    "Extension {} is annotated with @RestController, forwarding registration to request mapper",
                    extensionClass.getName()
                )
                val mapping =
                    springPluginManager.applicationContext.getBean<RequestHandlerMapping>("requestMappingHandlerMapping")

                mapping.registerExtension(extension)
            }
        } else {
            log.debug("Bean registeration aborted! Extension '{}' already existed as bean!", extensionClass.getName())
        }

    }
}