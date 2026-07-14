package com.pizzeria.internship.order_service.service;

import com.pizzeria.internship.order_service.repository.OrderItemRepository;

public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    OrderItemService(OrderItemRepository orderItemRepository){
        this.orderItemRepository = orderItemRepository;
    }
}
