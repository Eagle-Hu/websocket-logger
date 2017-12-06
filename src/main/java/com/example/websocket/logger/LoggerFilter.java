package com.example.websocket.logger;

import ch.qos.logback.classic.Level;
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
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by HuYanGuang on 2017/10/31.
 *
 * @author HuYanGuang
 */
@Component
public class LoggerFilter extends Filter {

    private static final Integer CLASS_NAME_MAX_LENGTH = 40;
    private static final Integer THREAD_NAME_MAX_LENGTH = 15;
    private static final Integer PROCESS_ID = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName()
            .split("@")[0]);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Function<Long, String> FORMAT_TIMESTAMP = timestamp -> {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return FORMATTER.format(dateTime);
    };
    private static final Function<Level, String> FORMAT_LEVEL = level -> String.format("%5s", level.levelStr)
            .replace(' ', '~');
    private static final Function<String, String> FORMAT_THREAD_NAME = threadName -> {
        if (StringUtils.isNotEmpty(threadName) && threadName.length() > THREAD_NAME_MAX_LENGTH) {
            return threadName.substring(threadName.length() - THREAD_NAME_MAX_LENGTH);
        }
        return String.format("%15s", threadName).replace(' ', '~');
    };
    private static final Function<String, String> FORMAT_CLASS_NAME = className -> {
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
    };
    private static final Consumer<StackTraceElementProxy> LOG_STACK_TRACE = stackTraceElementProxy -> {
        StackTraceElement stackTraceElement = stackTraceElementProxy.getStackTraceElement();
        LoggerMessage stackTrace = LoggerMessage.builder()
                .body("---- at ".concat(stackTraceElement.toString()))
                .build();
        LoggerQueue.getInstance().push(stackTrace);
    };

    @Override
    public FilterReply decide(Object o) {
        ILoggingEvent event = (ILoggingEvent) o;
        LoggerMessage loggerMessage = LoggerMessage.builder()
                .timestamp(FORMAT_TIMESTAMP.apply(event.getTimeStamp()))
                .level(FORMAT_LEVEL.apply(event.getLevel()))
                .processId(PROCESS_ID)
                .threadName(FORMAT_THREAD_NAME.apply(event.getThreadName()))
                .className(FORMAT_CLASS_NAME.apply(event.getLoggerName()))
                .body(event.getMessage())
                .build();
        LoggerQueue.getInstance().push(loggerMessage);

        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (Objects.nonNull(iThrowableProxy)) {
            LoggerMessage exception = LoggerMessage.builder()
                    .body(iThrowableProxy.getClassName().concat(": ").concat(iThrowableProxy.getMessage()))
                    .build();
            LoggerQueue.getInstance().push(exception);

            StackTraceElementProxy[] stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
            Stream.of(stackTraceElements).forEach(LOG_STACK_TRACE);

            iThrowableProxy = iThrowableProxy.getCause();
            while (Objects.nonNull(iThrowableProxy)) {
                exception = LoggerMessage.builder().body("Caused by: ".concat(iThrowableProxy.getClassName())
                        .concat(": ").concat(iThrowableProxy.getMessage())).build();
                LoggerQueue.getInstance().push(exception);

                stackTraceElements = iThrowableProxy.getStackTraceElementProxyArray();
                Stream.of(stackTraceElements).forEach(LOG_STACK_TRACE);

                iThrowableProxy = iThrowableProxy.getCause();
            }
        }
        return FilterReply.ACCEPT;
    }
}
