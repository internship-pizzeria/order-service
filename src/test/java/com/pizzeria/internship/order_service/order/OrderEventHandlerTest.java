package com.pizzeria.internship.order_service.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pizzeria.internship.order_service.security.OrderWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private OrderWebSocketHandler wsHandler;

    @InjectMocks
    private OrderEventHandler eventHandler;

    private static final Long TEST_LOCATION_ID = 1L;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void sendOrderNew_shouldSendToCorrectLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDto dto = new OrderResponseDto(
                orderId, "NEW", BigDecimal.TEN, "123 Main St", Instant.now(), List.of());

        eventHandler.sendOrderNew(TEST_LOCATION_ID, dto);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(wsHandler).sendToLocation(eq(TEST_LOCATION_ID), captor.capture());

        String json = captor.getValue();
        assertTrue(json.contains("\"eventType\":\"ORDER_NEW\""));
        assertTrue(json.contains(orderId.toString()));
    }

    @Test
    void sendStatusChanged_shouldSendToCorrectLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDto dto = new OrderResponseDto(
                orderId, "ACCEPTED", BigDecimal.TEN, "123 Main St", Instant.now(), List.of());

        eventHandler.sendStatusChanged(TEST_LOCATION_ID, dto);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(wsHandler).sendToLocation(eq(TEST_LOCATION_ID), captor.capture());

        String json = captor.getValue();
        assertTrue(json.contains("\"eventType\":\"ORDER_STATUS_CHANGED\""));
        assertTrue(json.contains("\"status\":\"ACCEPTED\""));
    }

    @Test
    void sendOrderNew_shouldUseCorrectLocationId() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDto dto = new OrderResponseDto(
                orderId, "NEW", BigDecimal.TEN, "123 Main St", Instant.now(), List.of());

        Long differentLocationId = 42L;
        eventHandler.sendOrderNew(differentLocationId, dto);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(wsHandler).sendToLocation(eq(differentLocationId), captor.capture());
        assertTrue(captor.getValue().contains("\"eventType\":\"ORDER_NEW\""));
    }
}
