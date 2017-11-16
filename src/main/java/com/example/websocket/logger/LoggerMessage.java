package com.example.websocket.logger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HuYanGuang on 2017/10/31.
 *
 * @author HuYanGuang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoggerMessage {

    private String body;

    private String timestamp;

    private String threadName;

    private String className;

    private String level;

    private Integer processId;
}

