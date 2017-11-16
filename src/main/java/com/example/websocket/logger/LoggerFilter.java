package com.example.websocket.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

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

        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (Objects.nonNull(iThrowableProxy)) {
            LoggerMessage exception = new LoggerMessage();
            exception.setBody(iThrowableProxy.getClassName().concat(": ").concat(iThrowableProxy.getMessage()));
            LoggerQueue.getInstance().push(exception);

            Consumer<StackTraceElementProxy> logException = stackTraceElementProxy -> {
                StackTraceElement stackTraceElement = stackTraceElementProxy.getStackTraceElement();
                LoggerMessage message = new LoggerMessage();
                message.setBody("---- at ".concat(stackTraceElement.toString()));
                LoggerQueue.getInstance().push(message);
            };
            StackTraceElementProxy[] stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
            Stream.of(stackTraceElements).forEach(logException);

            iThrowableProxy = iThrowableProxy.getCause();
            while (Objects.nonNull(iThrowableProxy)) {
                exception = new LoggerMessage();
                exception.setBody("Caused by: ".concat(iThrowableProxy.getClassName()).concat(": ")
                        .concat(iThrowableProxy.getMessage()));
                LoggerQueue.getInstance().push(exception);

                stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
                Stream.of(stackTraceElements).forEach(logException);

                iThrowableProxy = iThrowableProxy.getCause();
            }
        }
        return FilterReply.ACCEPT;
    }
}
