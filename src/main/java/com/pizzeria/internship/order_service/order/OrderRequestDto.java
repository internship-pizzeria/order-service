package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for creating a new order")
record OrderRequestDto(
        @Schema(description = "Customer name", example = "Jan Kowalski", requiredMode = Schema.RequiredMode.REQUIRED)
        String customerName,

        @Schema(description = "Customer phone number. Only digits, spaces, '+', '-' and parentheses are allowed. " +
                "The number is normalized before being stored.", example = "+48 123 456 789", requiredMode = Schema.RequiredMode.REQUIRED)
        String phoneNumber,

        @Schema(description = "Delivery address", example = "ul. Krakowska 10, Krakow", requiredMode = Schema.RequiredMode.REQUIRED)
        String deliveryAddress,

        @Schema(description = "Identifier of the location the order is placed at", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long locationId,

        @Schema(description = "Ordered items. The total quantity across all items must be between 1 and 50 pizzas.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<OrderItemRequestDto> items
) {

    public OrderRequestDto {
        if (items == null) items = List.of();
    }
}
