package com.pizzeria.internship.order_service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
class UserIdHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserIdHandshakeInterceptor.class);
    static final String ATTR_USER_ID = "userId";
    static final String ATTR_LOCATION_ID = "locationId";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String userIdHeader = request.getHeaders().getFirst(UserIdFilter.USER_ID_HEADER);
        String locationIdHeader = request.getHeaders().getFirst(UserIdFilter.LOCATION_ID_HEADER);

        if (userIdHeader == null || locationIdHeader == null) {
            log.warn("WebSocket handshake rejected: missing {} or {} header", UserIdFilter.USER_ID_HEADER, UserIdFilter.LOCATION_ID_HEADER);
            return false;
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            Long locationId = Long.parseLong(locationIdHeader);
            attributes.put(ATTR_USER_ID, userId);
            attributes.put(ATTR_LOCATION_ID, locationId);
            log.info("WebSocket handshake accepted: userId={}, locationId={}", userId, locationId);
            return true;
        } catch (NumberFormatException e) {
            log.warn("WebSocket handshake rejected: invalid header values: {}={}, {}={}",
                    UserIdFilter.USER_ID_HEADER, userIdHeader, UserIdFilter.LOCATION_ID_HEADER, locationIdHeader);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
