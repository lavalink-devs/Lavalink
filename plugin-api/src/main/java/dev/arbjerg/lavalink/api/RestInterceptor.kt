package dev.arbjerg.lavalink.api

import org.springframework.web.servlet.HandlerInterceptor

/**
 * This interface allows intercepting HTTP requests to the Lavalink server. Override the methods to add your own logic.
 */
interface RestInterceptor : HandlerInterceptor
