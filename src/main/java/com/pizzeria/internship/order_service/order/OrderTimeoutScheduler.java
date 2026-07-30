package com.pizzeria.internship.order_service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${order.timeout.minutes:15}")
    private long timeoutMinutes;

    @Scheduled(fixedRate = 60000)
    @SchedulerLock(name = "rejectExpiredOrders", lockAtMostFor = "5m", lockAtLeastFor = "1m")
    public void rejectExpiredOrders() {
        Instant timeout = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(Status.NEW, timeout);

        for (Order order : expiredOrders) {
            try {
                rejectOrder(order);
            } catch (Exception e) {
                log.error("Failed to auto-reject order {} for location {}", order.getId(), order.getLocationId(), e);
            }
        }
    }

    @Transactional
    void rejectOrder(Order order) {
        try {
            order.reject("Order timed out - no response from location");
            orderRepository.save(order);
            log.info("Auto-rejected order {} for location {} - no response within {} minutes",
                    order.getId(), order.getLocationId(), timeoutMinutes);
            OrderResponseDto dto = OrderResponseDto.fromOrder(order);
            eventPublisher.publishEvent(new OrderEvent("ORDER_STATUS_CHANGED", order.getLocationId(), null, dto));
        } catch (OptimisticLockingFailureException e) {
            log.info("Skipping order {} - concurrently updated by another request", order.getId());
        }
    }
}
