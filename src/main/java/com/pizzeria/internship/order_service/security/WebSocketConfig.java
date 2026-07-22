package com.pizzeria.internship.order_service.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private final OrderWebSocketHandler orderWebSocketHandler;
    private final UserIdHandshakeInterceptor handshakeInterceptor;

    WebSocketConfig(OrderWebSocketHandler orderWebSocketHandler,
                    UserIdHandshakeInterceptor handshakeInterceptor) {
        this.orderWebSocketHandler = orderWebSocketHandler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(handshakeInterceptor);
    }
}
