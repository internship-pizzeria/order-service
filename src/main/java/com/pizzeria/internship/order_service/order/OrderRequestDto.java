package com.pizzeria.internship.order_service.order;

import java.util.List;

record OrderRequestDto(String customerName, String phoneNumber, String deliveryAddress, Long locationId, List<OrderItemRequestDto> items) {

    public OrderRequestDto {
        if (items == null) items = List.of();
    }
}
