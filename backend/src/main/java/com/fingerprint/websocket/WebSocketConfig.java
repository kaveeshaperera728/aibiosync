package com.fingerprint.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // Increase buffer to 8MB (default is 8KB) to handle massive getuserlist JSON arrays
        container.setMaxTextMessageBufferSize(8192 * 1024);
        container.setMaxBinaryMessageBufferSize(8192 * 1024);
        return container;
    }

    private final DeviceWebSocketHandler deviceWebSocketHandler;

    public WebSocketConfig(DeviceWebSocketHandler deviceWebSocketHandler) {
        this.deviceWebSocketHandler = deviceWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the handler on multiple common paths, importantly /pub/chat which the device targets
        registry.addHandler(deviceWebSocketHandler, "/", "/ws", "/websocket", "/iclock/cdata", "/pub/chat")
                .setAllowedOrigins("*");
    }
}
