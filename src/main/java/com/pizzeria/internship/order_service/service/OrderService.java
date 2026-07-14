package com.pizzeria.internship.order_service.service;

import com.pizzeria.internship.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    OrderService(OrderRepository orderRepository){
        this.orderRepository=orderRepository;
    }

}
