package com.pizzeria.internship.order_service.order;

public record OrderEvent(String eventType, Long locationId, OrderResponseDto data) {
}
