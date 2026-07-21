package com.pizzeria.internship.order_service.order;

import java.util.UUID;

public class OrderAccessDeniedException extends RuntimeException {

    public OrderAccessDeniedException(UUID orderId) {
        super("Access denied: order " + orderId + " does not belong to your location");
    }
}
