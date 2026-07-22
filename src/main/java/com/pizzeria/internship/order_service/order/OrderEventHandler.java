package com.pizzeria.internship.order_service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pizzeria.internship.order_service.security.OrderWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final OrderWebSocketHandler wsHandler;
    private final ObjectMapper objectMapper;

    OrderEventHandler(OrderWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    void sendOrderNew(Long locationId, OrderResponseDto order) {
        sendEvent(locationId, "ORDER_NEW", order);
    }

    void sendStatusChanged(Long locationId, OrderResponseDto order) {
        sendEvent(locationId, "ORDER_STATUS_CHANGED", order);
    }

    private void sendEvent(Long locationId, String eventType, OrderResponseDto order) {
        try {
            OrderEvent event = new OrderEvent(eventType, order);
            String json = objectMapper.writeValueAsString(event);
            log.info("Sending {} event to locationId={} for orderId={}", eventType, locationId, order.orderId());
            wsHandler.sendToLocation(locationId, json);
        } catch (Exception e) {
            log.error("Failed to send {} event to locationId={}", eventType, locationId, e);
        }
    }
}
