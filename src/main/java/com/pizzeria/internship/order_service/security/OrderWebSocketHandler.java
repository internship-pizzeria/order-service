package com.pizzeria.internship.order_service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderWebSocketHandler.class);

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByLocation = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long locationId = (Long) session.getAttributes().get(UserIdHandshakeInterceptor.ATTR_LOCATION_ID);
        if (locationId == null) {
            log.warn("WebSocket connected without locationId, closing session");
            try {
                session.close();
            } catch (IOException e) {
                log.error("Error closing session", e);
            }
            return;
        }
        sessionsByLocation.computeIfAbsent(locationId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("WebSocket connected: sessionId={}, locationId={}, total={}",
                session.getId(), locationId, sessionsByLocation.get(locationId).size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long locationId = (Long) session.getAttributes().get(UserIdHandshakeInterceptor.ATTR_LOCATION_ID);
        if (locationId != null) {
            Set<WebSocketSession> sessions = sessionsByLocation.get(locationId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionsByLocation.remove(locationId);
                }
                log.info("WebSocket disconnected: sessionId={}, locationId={}, remaining={}",
                        session.getId(), locationId,
                        sessionsByLocation.containsKey(locationId) ? sessionsByLocation.get(locationId).size() : 0);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    public void sendToLocation(Long locationId, String payload) {
        Set<WebSocketSession> sessions = sessionsByLocation.get(locationId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("No WebSocket clients connected for locationId={}", locationId);
            return;
        }
        TextMessage message = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    log.error("Failed to send message to sessionId={}, locationId={}", session.getId(), locationId, e);
                }
            }
        }
    }
}
