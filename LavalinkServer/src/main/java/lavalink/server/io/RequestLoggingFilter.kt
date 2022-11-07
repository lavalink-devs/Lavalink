package lavalink.server.io

import org.slf4j.LoggerFactory
import org.springframework.web.filter.AbstractRequestLoggingFilter
import javax.servlet.http.HttpServletRequest

class RequestLoggingFilter : AbstractRequestLoggingFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)
    }

    init {
        isIncludeClientInfo = true
        isIncludeQueryString = true
        isIncludePayload = true
        maxPayloadLength = 10000
        isIncludeHeaders = false
        setAfterMessagePrefix("")
    }

    override fun beforeRequest(request: HttpServletRequest, message: String) {}

    override fun afterRequest(request: HttpServletRequest, message: String) {
        log.info(message)
    }
}