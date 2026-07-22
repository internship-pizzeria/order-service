package com.pizzeria.internship.order_service.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderTimeoutSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderTimeoutScheduler orderTimeoutScheduler;

    @Test
    void shouldRejectExpiredOrders() {
        // GIVEN
        ReflectionTestUtils.setField(orderTimeoutScheduler, "timeoutMinutes", 15L);

        Order expiredOrder = Order.builder()
                .id(UUID.randomUUID())
                .customerName("Jan")
                .phoneNumber("123456789")
                .deliveryAddress("ul. Testowa 1")
                .locationId(1L)
                .status(Status.NEW)
                .totalPrice(BigDecimal.valueOf(25.00))
                .build();

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(Status.NEW), any(Instant.class)))
                .thenReturn(List.of(expiredOrder));

        // WHEN
        orderTimeoutScheduler.rejectExpiredOrders();

        // THEN
        verify(orderRepository, times(1)).save(expiredOrder);
        assert expiredOrder.getStatus() == Status.REJECTED;
        assert expiredOrder.getRejectionReason().equals("Order timed out - no response from location");
    }

    @Test
    void shouldNotCallSaveWhenNoExpiredOrders() {
        // GIVEN
        ReflectionTestUtils.setField(orderTimeoutScheduler, "timeoutMinutes", 15L);

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(Status.NEW), any(Instant.class)))
                .thenReturn(List.of());

        // WHEN
        orderTimeoutScheduler.rejectExpiredOrders();

        // THEN
        verify(orderRepository, never()).save(any(Order.class));
    }
}