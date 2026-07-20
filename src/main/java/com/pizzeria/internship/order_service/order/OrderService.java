package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.product.Product;
import com.pizzeria.internship.order_service.product.ProductClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
class OrderService {
    private static final int MIN_PIZZAS_PER_ORDER = 1;
    private static final int MAX_PIZZAS_PER_ORDER = 50;
    private static final String PHONE_NUMBER_REGEX = "^[+\\d\\s\\-()]+$";
    private static final List<Status> ACTIVE_STATUSES = List.of(Status.NEW, Status.ACCEPTED, Status.IN_PROGRESS);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    Order createOrder(OrderRequestDto request) {
        validateRequest(request);
        Order order = buildOrderFromRequest(request);
        request.items().forEach(item -> addItem(order, item.productId(), item.quantity()));
        order.calculateTotalPrice();
        return orderRepository.save(order);
    }

    List<OrderResponseDto> getOrdersByPhoneNumber(String phoneNumber) {
        List<Order> orders = orderRepository.findByPhoneNumberAndStatusIn(normalizePhoneNumber(phoneNumber), ACTIVE_STATUSES);
        if (orders.isEmpty()) {
            throw new OrderNotFoundException(phoneNumber);
        }
        return orders.stream()
                .map(OrderResponseDto::fromOrder)
                .toList();
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
                .phoneNumber(normalizePhoneNumber(request.phoneNumber()))
                .deliveryAddress(request.deliveryAddress())
                .locationId(request.locationId())
                .status(Status.NEW)
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "");
    }

    private void addItem(Order order, Long productId, int quantity) {
        Product product = productClient.getProductById(productId);
        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .order(order)
                .quantity(quantity)
                .historicalName(product.getName())
                .historicalPrice(product.getPrice())
                .build();
        order.addItem(item);
    }
}
