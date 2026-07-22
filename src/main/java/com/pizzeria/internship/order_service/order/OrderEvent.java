package com.pizzeria.internship.order_service.order;

record OrderEvent(String eventType, Long locationId, OrderResponseDto data) {
}
