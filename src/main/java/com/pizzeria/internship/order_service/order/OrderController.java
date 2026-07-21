package com.pizzeria.internship.order_service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponseDto createOrder(@RequestBody OrderRequestDto orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping
    List<OrderResponseDto> getOrdersByLocation(
            @RequestParam(required = false) String status) {
        Status statusFilter = (status != null) ? Status.valueOf(status.toUpperCase()) : null;
        return orderService.getOrdersByLocation(statusFilter);
    }

    @GetMapping("/{orderId}/status")
    OrderResponseDto getOrderStatus(@PathVariable UUID orderId) {
        return orderService.getOrderStatusById(orderId);
    }

    @PatchMapping("/{orderId}/status")
    OrderResponseDto updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdateStatusRequestDto request) {
        return orderService.updateOrderStatus(orderId, request);
    }
}
