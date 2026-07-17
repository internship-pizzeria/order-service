package com.pizzeria.internship.order_service.order;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String phoneNumber) {
        super("No orders found for phone number: " + phoneNumber);
    }
}
