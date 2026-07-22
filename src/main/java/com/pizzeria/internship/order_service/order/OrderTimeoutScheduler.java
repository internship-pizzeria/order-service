package com.pizzeria.internship.order_service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${order.timeout.minutes:15}")
    private long timeoutMinutes;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void rejectExpiredOrders() {
        Instant timeout = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(Status.NEW, timeout);

        for (Order order : expiredOrders) {
            order.reject("Order timed out - no response from location");
            orderRepository.save(order);
            log.info("Auto-rejected order {} for location {} - no response within {} minutes",
                    order.getId(), order.getLocationId(), timeoutMinutes);
        }
    }
}
