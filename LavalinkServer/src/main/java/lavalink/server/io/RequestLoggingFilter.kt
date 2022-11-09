package lavalink.server.io

import lavalink.server.config.RequestLoggingConfig
import org.slf4j.LoggerFactory
import org.springframework.web.filter.AbstractRequestLoggingFilter
import javax.servlet.http.HttpServletRequest

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
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {}

    override fun afterRequest(request: HttpServletRequest, message: String) {
        log.info(message)
    }
}