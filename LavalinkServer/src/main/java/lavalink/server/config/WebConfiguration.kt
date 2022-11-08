package lavalink.server.config

import dev.arbjerg.lavalink.protocol.objectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebConfiguration(private val interceptors: List<HandlerInterceptor>) : WebMvcConfigurer {

    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val builder = Jackson2ObjectMapperBuilder()
        builder.configure(objectMapper())
        converters.add(StringHttpMessageConverter())
        converters.add(MappingJackson2HttpMessageConverter(builder.build()))
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        interceptors.forEach { registry.addInterceptor(it) }
    }

}