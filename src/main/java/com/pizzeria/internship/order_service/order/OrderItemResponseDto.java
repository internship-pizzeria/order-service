package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "A single line item of an order with the product data snapshot taken at order time")
public record OrderItemResponseDto(
        @Schema(description = "Identifier of the product in the catalog-service", example = "1")
        Long productId,

        @Schema(description = "Product name as it was at the moment of ordering", example = "Margherita")
        String historicalName,

        @Schema(description = "Unit price of the product at the moment of ordering", example = "25.00")
        BigDecimal historicalPrice,

        @Schema(description = "Ordered quantity", example = "2")
        Integer quantity
) {

    static OrderItemResponseDto fromOrderItem(OrderItem item) {
        return new OrderItemResponseDto(
                item.getProductId(),
                item.getHistoricalName(),
                item.getHistoricalPrice(),
                item.getQuantity()
        );
    }
}
