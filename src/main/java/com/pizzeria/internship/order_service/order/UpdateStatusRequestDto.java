package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for changing the status of an order")
record UpdateStatusRequestDto(
        @Schema(
                description = "Target status. Only valid transitions are accepted, e.g. NEW -> ACCEPTED, " +
                        "READY -> PAID, READY -> IN_DELIVERY. Value is case-insensitive.",
                example = "ACCEPTED",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"NEW", "ACCEPTED", "IN_PROGRESS", "REJECTED", "READY", "PAID", "IN_DELIVERY", "DELIVERED"}
        )
        String status
) {
}
