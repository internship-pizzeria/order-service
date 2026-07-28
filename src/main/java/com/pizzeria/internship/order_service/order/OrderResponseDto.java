package com.pizzeria.internship.order_service.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID orderId,
        String status,
        BigDecimal totalPrice,
        String deliveryAddress,
        Instant createdAt,
        List<OrderItemResponseDto> items
) {

    static OrderResponseDto fromOrder(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponseDto::fromOrderItem)
                        .toList()
        );
    }
}
