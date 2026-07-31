package com.pizzeria.internship.order_service.order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of an order", enumAsRef = true)
public enum Status {
    NEW,
    ACCEPTED,
    IN_PROGRESS,
    REJECTED,
    READY,
    PAID,
    IN_DELIVERY,
    DELIVERED
}
