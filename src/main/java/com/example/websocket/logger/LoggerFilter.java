package com.example.websocket.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
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

    private static final Integer CLASS_NAME_MAX_LENGTH = 40;
    private static final Integer PROCESS_ID = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName()
            .split("@")[0]);

    @Override
    public FilterReply decide(Object o) {
        ILoggingEvent event = (ILoggingEvent) o;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()),
                ZoneId.systemDefault());
        LoggerMessage loggerMessage = LoggerMessage.builder()
                .timestamp(formatter.format(dateTime))
                .level(String.format("%5s", event.getLevel().levelStr))
                .processId(PROCESS_ID)
                .threadName(String.format("%15s", event.getThreadName()).replace(' ', '~'))
                .className(getFormattedClassName(event.getLoggerName()))
                .body(event.getMessage())
                .build();
        LoggerQueue.getInstance().push(loggerMessage);

        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (Objects.nonNull(iThrowableProxy)) {
            LoggerMessage exception = LoggerMessage.builder()
                    .body(iThrowableProxy.getClassName().concat(": ").concat(iThrowableProxy.getMessage()))
                    .build();
            LoggerQueue.getInstance().push(exception);

            Consumer<StackTraceElementProxy> logException = stackTraceElementProxy -> {
                StackTraceElement stackTraceElement = stackTraceElementProxy.getStackTraceElement();
                LoggerMessage message = LoggerMessage.builder()
                        .body("---- at ".concat(stackTraceElement.toString()))
                        .build();
                LoggerQueue.getInstance().push(message);
            };
            StackTraceElementProxy[] stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
            Stream.of(stackTraceElements).forEach(logException);

            iThrowableProxy = iThrowableProxy.getCause();
            while (Objects.nonNull(iThrowableProxy)) {
                exception = LoggerMessage.builder().body("Caused by: ".concat(iThrowableProxy.getClassName())
                        .concat(": ").concat(iThrowableProxy.getMessage())).build();
                LoggerQueue.getInstance().push(exception);

                stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
                Stream.of(stackTraceElements).forEach(logException);

                iThrowableProxy = iThrowableProxy.getCause();
            }
        }
        return FilterReply.ACCEPT;
    }

    private String getFormattedClassName(String className) {
        if (StringUtils.isNotEmpty(className) && className.length() > CLASS_NAME_MAX_LENGTH) {
            StringBuilder builder = new StringBuilder(className);
            int index = 1;
            boolean outer;
            boolean inner;
            do {
                inner = builder.charAt(index) != '.' && builder.charAt(index) != '$';
                while (inner) {
                    builder.delete(index, index + 1);
                    inner = builder.charAt(index) != '.' && builder.charAt(index) != '$';
                }
                index += 2;
                outer = index < builder.length() && (builder.length() > CLASS_NAME_MAX_LENGTH
                        && (builder.indexOf(".", index) > 0 || builder.indexOf("$", index) > 0));
            } while (outer);
            index = Math.min(Math.max(builder.length() - CLASS_NAME_MAX_LENGTH, 0),
                    Math.max(builder.lastIndexOf("."), builder.lastIndexOf("$")) + 1);
            className = builder.substring(index);
        }
        return String.format("%40s", className).replace(' ', '~');
    }
}
