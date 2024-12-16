package lavalink.server.config

import dev.arbjerg.lavalink.api.RestInterceptor
import dev.arbjerg.lavalink.protocol.v4.json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.accept.ContentNegotiationManager
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.resource.ResourceUrlProvider

class RequestHandlerMapping : RequestMappingHandlerMapping() {
    fun registerExtension(extension: Any) = detectHandlerMethods(extension)
}

@Configuration
class WebConfiguration(private val interceptors: List<RestInterceptor>) : DelegatingWebMvcConfiguration() {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(StringHttpMessageConverter())
        converters.add(KotlinSerializationJsonHttpMessageConverter(json))
        converters.add(MappingJackson2HttpMessageConverter())
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        interceptors.forEach { registry.addInterceptor(it) }
    }

    override fun createRequestMappingHandlerMapping(): RequestMappingHandlerMapping = RequestHandlerMapping()

    @Primary
    @Bean
    override fun requestMappingHandlerMapping(
        contentNegotiationManager: ContentNegotiationManager,
        conversionService: FormattingConversionService,
        resourceUrlProvider: ResourceUrlProvider
    ): RequestMappingHandlerMapping = RequestHandlerMapping().apply {
        this.contentNegotiationManager = contentNegotiationManager
    }
}
