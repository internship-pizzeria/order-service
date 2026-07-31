package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "An order with its current status, calculated total and line items")
public record OrderResponseDto(
        @Schema(description = "Unique identifier of the order", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID orderId,

        @Schema(description = "Current status of the order",
                example = "NEW",
                allowableValues = {"NEW", "ACCEPTED", "IN_PROGRESS", "REJECTED", "READY", "PAID", "IN_DELIVERY", "DELIVERED"})
        String status,

        @Schema(description = "Total price of the order calculated from the item price snapshots", example = "62.50")
        BigDecimal totalPrice,

        @Schema(description = "Delivery address", example = "ul. Krakowska 10, Krakow")
        String deliveryAddress,

        @Schema(description = "Timestamp when the order was created", example = "2026-07-31T10:15:30Z")
        Instant createdAt,

        @Schema(description = "Line items of the order")
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
