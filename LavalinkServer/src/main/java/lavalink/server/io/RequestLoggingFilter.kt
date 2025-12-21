package lavalink.server.io

import jakarta.servlet.http.HttpServletRequest
import lavalink.server.config.RequestLoggingConfig
import org.slf4j.LoggerFactory
import org.springframework.web.filter.AbstractRequestLoggingFilter

class RequestLoggingFilter(
    private val requestLoggingConfig: RequestLoggingConfig
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
        setBeforeMessagePrefix(">> ")
        setBeforeMessageSuffix("")
        setAfterMessagePrefix("")
        setAfterMessageSuffix("")
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {
        if (!requestLoggingConfig.beforeRequest) {
            return
        }
        log.info(message)
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        log.info(message)
    }
}