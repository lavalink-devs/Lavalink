package lavalink.client;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sedmelluq
 */
public class SimpleLogToSLF4JAdapter implements SimpleLog.LogListener {
    private static final Logger log = LoggerFactory.getLogger(JDA.class);

    @Override
    public void onLog(SimpleLog simpleLog, SimpleLog.Level logLevel, Object message) {
        switch (logLevel) {
            case TRACE:
                if (log.isTraceEnabled()) {
                    log.trace(message.toString());
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(message.toString());
                }
                break;
            case INFO:
                log.info(message.toString());
                break;
            case WARNING:
                log.warn(message.toString());
                break;
            case FATAL:
                log.error(message.toString());
                break;
        }
    }

    @Override
    public void onError(SimpleLog simpleLog, Throwable err) {
        log.error("An exception occurred", err);
    }
}
