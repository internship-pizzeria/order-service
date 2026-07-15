package com.pizzeria.internship.order_service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Order createOrder(@RequestBody OrderRequestDto orderRequest){
        return orderService.createOrder(orderRequest);
    }
}
