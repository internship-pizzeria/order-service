package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.order.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
class AnalyticsSyncHandler {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsSyncHandler.class);

    private final JdbcTemplate analyticsJdbcTemplate;

    AnalyticsSyncHandler(JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    void onOrderCreated(OrderEvent event) {
        if (!"ORDER_NEW".equals(event.eventType())) {
            return;
        }

        try {
            var dto = event.data();
            for (var item : dto.items()) {
                BigDecimal itemTotal = item.historicalPrice()
                        .multiply(BigDecimal.valueOf(item.quantity()));
                analyticsJdbcTemplate.update("""
                        INSERT INTO report_order_items
                        (order_id, location_id, product_id, product_name, quantity, unit_price, total_price, status, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        dto.orderId(),
                        event.locationId(),
                        item.productId(),
                        item.historicalName(),
                        item.quantity(),
                        item.historicalPrice(),
                        itemTotal,
                        dto.status(),
                        dto.createdAt()
                );
            }
            log.info("Synced order {} to analytics DB ({} items)", dto.orderId(), dto.items().size());
        } catch (Exception e) {
            log.error("Failed to sync order to analytics DB", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = AFTER_COMMIT)
    void onOrderStatusChanged(OrderEvent event) {
        if (!"ORDER_STATUS_CHANGED".equals(event.eventType())) {
            return;
        }

        try {
            int updated = analyticsJdbcTemplate.update("""
                    UPDATE report_order_items
                    SET status = ?
                    WHERE order_id = ?
                    """,
                    event.data().status(),
                    event.data().orderId()
            );
            log.info("Updated status for order {} in analytics DB ({} rows)", event.data().orderId(), updated);
        } catch (Exception e) {
            log.error("Failed to update order status in analytics DB", e);
        }
    }
}