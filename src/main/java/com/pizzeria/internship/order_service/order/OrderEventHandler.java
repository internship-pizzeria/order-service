package com.pizzeria.internship.order_service.order;

import tools.jackson.databind.ObjectMapper;
import com.pizzeria.internship.order_service.security.OrderWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final OrderWebSocketHandler wsHandler;
    private final ObjectMapper objectMapper;

    OrderEventHandler(OrderWebSocketHandler wsHandler, ObjectMapper objectMapper) {
        this.wsHandler = wsHandler;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    void onOrderEvent(OrderEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("Sending {} event to locationId={} for orderId={}",
                    event.eventType(), event.locationId(), event.data().orderId());
            wsHandler.sendToLocation(event.locationId(), json);
        } catch (Exception e) {
            log.error("Failed to send {} event to locationId={}", event.eventType(), event.locationId(), e);
        }
    }
}
