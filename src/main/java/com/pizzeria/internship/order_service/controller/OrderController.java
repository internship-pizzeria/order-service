package com.pizzeria.internship.order_service.controller;


import com.pizzeria.internship.order_service.dto.OrderRequest;
import com.pizzeria.internship.order_service.service.OrderService;
import com.pizzeria.internship.order_service.entity.Order;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/api/v1/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest){
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.ACCEPTED);

    }
}
