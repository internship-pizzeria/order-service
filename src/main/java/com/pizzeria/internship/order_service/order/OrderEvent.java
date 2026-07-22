package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.order.OrderResponseDto;

record OrderEvent(String eventType, OrderResponseDto data) {
}
