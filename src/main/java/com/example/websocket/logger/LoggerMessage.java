package com.example.websocket.logger;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by HuYanGuang on 2017/10/31.
 *
 * @author HuYanGuang
 */
public class LoggerMessage {

    private String body;

    private String timestamp;

    private String threadName;

    private String className;

    private String level;

    public LoggerMessage() {
    }

    public LoggerMessage(String body, String timestamp, String threadName, String className, String level) {
        this.body = body;
        this.timestamp = timestamp;
        this.threadName = threadName;
        this.className = className;
        this.level = level;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getFormattedClassName() {
        String formattedClassName = className;
        if (Objects.nonNull(className) && className.length() >= 40) {
            String[] packages = className.split("\\.");
            formattedClassName = Stream.of(packages).limit(packages.length - 1).map(s -> s.substring(0, 1))
                    .collect(Collectors.joining(".")).concat(".").concat(packages[packages.length - 1]);
        }
        return formattedClassName;
    }
}

