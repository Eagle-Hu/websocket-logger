package com.example.websocket.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by HuYanGuang on 2017/10/31.
 *
 * @author HuYanGuang
 */
@Component
public class LoggerFilter extends Filter {

    @Override
    public FilterReply decide(Object o) {
        ILoggingEvent event = (ILoggingEvent) o;
        LoggerMessage loggerMessage = new LoggerMessage(event.getMessage(),
                DateFormat.getDateTimeInstance().format(new Date(event.getTimeStamp())), event.getThreadName(),
                event.getLoggerName(), event.getLevel().levelStr);
        LoggerQueue.getInstance().push(loggerMessage);
        return FilterReply.ACCEPT;
    }
}
