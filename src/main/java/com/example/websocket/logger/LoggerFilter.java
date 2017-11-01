package com.example.websocket.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()),
                ZoneId.systemDefault());
        LoggerMessage loggerMessage = new LoggerMessage(event.getMessage(), formatter.format(dateTime),
                event.getThreadName(), event.getLoggerName(), event.getLevel().levelStr);
        LoggerQueue.getInstance().push(loggerMessage);
        return FilterReply.ACCEPT;
    }
}
