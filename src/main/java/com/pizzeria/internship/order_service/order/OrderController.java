package com.pizzeria.internship.order_service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequestDto orderRequest){
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.ACCEPTED);
    }
}
