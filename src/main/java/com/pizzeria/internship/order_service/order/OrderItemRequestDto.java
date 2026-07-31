package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single product line requested in an order")
record OrderItemRequestDto(
        @Schema(description = "Identifier of the product in the catalog-service", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productId,

        @Schema(description = "Quantity of the product. Must be at least 1.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
        int quantity
) {

}
