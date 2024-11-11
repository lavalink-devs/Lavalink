package lavalink.server.bootstrap

import lavalink.server.config.RequestHandlerMapping
import org.pf4j.spring.ExtensionsInjector
import org.pf4j.spring.SpringPluginManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.full.hasAnnotation

private val log = LoggerFactory.getLogger(LavalinkExtensionInjector::class.java)

class LavalinkExtensionInjector(pluginManager: SpringPluginManager, factory: AbstractAutowireCapableBeanFactory) : ExtensionsInjector(pluginManager, factory) {

    override fun registerExtension(extensionClass: Class<*>) {
        val extensionBeanMap = springPluginManager.applicationContext.getBeansOfType(extensionClass);
        if (extensionBeanMap.isEmpty()) {
            val extension = springPluginManager.getExtensionFactory().create(extensionClass);

            this.beanFactory.registerSingleton(extensionClass.getName(), extension);
            this.beanFactory.autowireBean(extension);

            if (extension::class.hasAnnotation<RestController>()) {
                log.debug("Extension {} is annotated with @RestController, forwarding registration to request mapper", extensionClass.getName())
                val mapping = springPluginManager.applicationContext.getBean<RequestHandlerMapping>("requestMappingHandlerMapping")

                mapping.registerExtension(extension)
            }

        } else {
            log.debug("Bean registeration aborted! Extension '{}' already existed as bean!", extensionClass.getName());
        }

    }
}