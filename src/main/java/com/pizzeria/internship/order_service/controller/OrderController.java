package com.pizzeria.internship.order_service.controller;


import com.pizzeria.internship.order_service.service.OrderService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService){
        this.orderService = orderService;
    }
}
