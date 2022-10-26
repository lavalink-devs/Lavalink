package dev.arbjerg.lavalink.protocol

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

fun newObjectMapper(): ObjectMapper {
    val module = SimpleModule()
        .addDeserializer(Message::class.java, MessageDeserializer())
        .addDeserializer(Message.Event::class.java, Message.EventDeserializer())

    return ObjectMapper().
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
        registerKotlinModule().
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY).
        registerModule(module)
}