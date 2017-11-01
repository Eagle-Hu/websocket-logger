package com.example.websocket.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Created by HuYanGuang on 2017/10/31.
 *
 * @author HuYanGuang
 */
@Configuration
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private String allowedHost;

    /**
     * 注意：为了连接安全，setAllowedOrigins设置的允许连接的源地址，如果在非这个配置的地址下发起连接会报403，
     * 进一步还可以使用addInterceptors设置拦截器，来做相关的鉴权操作
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/webSocket").setAllowedOrigins(allowedHost).withSockJS();
    }

    @Bean
    public String getAllowedHost(@Value("${server.port}") String serverPort, @Value("${app.ip}") String appIp) {
        return "http://".concat(appIp).concat(":").concat(serverPort);
    }
}
