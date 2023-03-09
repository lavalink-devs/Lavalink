package lavalink.server.config

import dev.arbjerg.lavalink.api.RestInterceptor
import dev.arbjerg.lavalink.protocol.v3.objectMapper
import dev.arbjerg.lavalink.protocol.v4.json
import lavalink.server.io.KotlinxSerialization2HttpMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebConfiguration(private val interceptors: List<RestInterceptor>) : WebMvcConfigurer {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val builder = Jackson2ObjectMapperBuilder()
        builder.configure(objectMapper())
        converters.add(StringHttpMessageConverter())
        converters.add(KotlinxSerialization2HttpMessageConverter(json))
        converters.add(MappingJackson2HttpMessageConverter(builder.build()))
    }

    @Bean
    fun jackson2ObjectMapperBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        builder.configure(objectMapper())
        return builder
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        interceptors.forEach { registry.addInterceptor(it) }
    }

}
