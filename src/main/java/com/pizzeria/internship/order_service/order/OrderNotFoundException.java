package com.pizzeria.internship.order_service.order;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String phoneNumber) {
        super("No orders found for phone number: " + phoneNumber);
    }

    public OrderNotFoundException(UUID orderId) {
        super("Order not found with id: " + orderId);
    }
}
