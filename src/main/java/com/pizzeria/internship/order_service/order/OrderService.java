package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.product.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
class OrderService {
    private static final int MIN_PIZZAS_PER_ORDER = 1;
    private static final int MAX_PIZZAS_PER_ORDER = 50;
    private static final String PHONE_NUMBER_REGEX = "^[+\\d\\s\\-()]+$";

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    Order createOrder(OrderRequestDto request) {
        validateRequest(request);
        Order order = buildOrderFromRequest(request);
        request.items().forEach(item -> addItem(order, item.productId(), item.quantity(), request.locationId()));
        order.calculateTotalPrice();
        return orderRepository.save(order);
    }

    private void validateRequest(OrderRequestDto request) {
        validatePhoneNumber(request.phoneNumber());
        validateQuantity(request.items());
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches(PHONE_NUMBER_REGEX)) {
            throw new InvalidOrderException("Invalid phone number format: " + phoneNumber);
        }
    }

    private void validateQuantity(java.util.List<OrderItemRequestDto> items) {
        int totalQuantity = items.stream()
                .mapToInt(OrderItemRequestDto::quantity)
                .sum();
        if  (totalQuantity<MIN_PIZZAS_PER_ORDER){
            throw new InvalidOrderException(
                    "You cannot order less than" + MIN_PIZZAS_PER_ORDER + "pizzas per order");
        }
        if (totalQuantity > MAX_PIZZAS_PER_ORDER) {
            throw new InvalidOrderException(
                    "Total quantity exceeds limit of " + MAX_PIZZAS_PER_ORDER + " pizzas per order");
        }
    }

    private Order buildOrderFromRequest(OrderRequestDto request) {
        return Order.builder()
                .customerName(request.customerName())
                .phoneNumber(request.phoneNumber())
                .deliveryAddress(request.deliveryAddress())
                .locationId(request.locationId())
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    private void addItem(Order order, Long productId, int quantity, Long locationId) {
        ProductDto product = productClient.getProductById(productId, locationId);
        OrderItem item = OrderItem.builder()
                .productId(product.id())
                .order(order)
                .quantity(quantity)
                .historicalName(product.name())
                .historicalPrice(product.price())
                .build();
        order.addItem(item);
    }
}
