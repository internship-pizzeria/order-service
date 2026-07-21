package com.pizzeria.internship.order_service.order;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(Status from, Status to) {
        super("Invalid status transition from " + from + " to " + to);
    }
}
