package com.pizzeria.internship.order_service.order;

import java.math.BigDecimal;

public record OrderItemResponseDto(Long productId, String historicalName, BigDecimal historicalPrice, Integer quantity) {

    static OrderItemResponseDto fromOrderItem(OrderItem item) {
        return new OrderItemResponseDto(
                item.getProductId(),
                item.getHistoricalName(),
                item.getHistoricalPrice(),
                item.getQuantity()
        );
    }
}
