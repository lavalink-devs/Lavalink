package lavalink.server.io

import jakarta.servlet.http.HttpServletRequest
import lavalink.server.config.RequestLoggingConfig
import org.slf4j.LoggerFactory
import org.springframework.web.filter.AbstractRequestLoggingFilter

class RequestLoggingFilter(
    requestLoggingConfig: RequestLoggingConfig
) : AbstractRequestLoggingFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
    }

    init {
        isIncludeClientInfo = requestLoggingConfig.includeClientInfo
        isIncludeHeaders = requestLoggingConfig.includeHeaders
        isIncludeQueryString = requestLoggingConfig.includeQueryString
        isIncludePayload = requestLoggingConfig.includePayload
        maxPayloadLength = requestLoggingConfig.maxPayloadLength
        setAfterMessagePrefix("")
        setAfterMessageSuffix("")
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {}

    override fun afterRequest(request: HttpServletRequest, message: String) {
        log.info(message)
    }
}