package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.analytics.revenue.RevenueCacheService;
import com.pizzeria.internship.order_service.product.Product;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class OrderService {
    private static final int MIN_PIZZAS_PER_ORDER = 1;
    private static final int MAX_PIZZAS_PER_ORDER = 50;
    private static final String PHONE_NUMBER_REGEX = "^[+\\d\\s\\-()]+$";

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RevenueCacheService revenueCacheService;

    @Transactional
    OrderResponseDto createOrder(OrderRequestDto request) {
        validateRequest(request);
        Order order = buildOrderFromRequest(request);

        List<Long> productIds = request.items().stream()
                .map(OrderItemRequestDto::productId)
                .toList();
        Map<Long, Product> products = productClient.getProductsByIds(productIds, request.locationId());

        for (OrderItemRequestDto item : request.items()) {
            Product product = products.get(item.productId());
            if (product == null || !product.isAvailable()) {
                throw new InvalidOrderException(
                        "Product " + item.productId() + " is currently unavailable");
            }
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .order(order)
                    .quantity(item.quantity())
                    .historicalName(product.getName())
                    .historicalPrice(product.getPrice())
                    .build();
            order.addItem(orderItem);
        }

        order.calculateTotalPrice();
        orderRepository.save(order);
        revenueCacheService.addOrderRevenue(order.getLocationId(), order.getTotalPrice());
        OrderResponseDto dto = OrderResponseDto.fromOrder(order);
        eventPublisher.publishEvent(new OrderEvent("ORDER_NEW", order.getLocationId(), UserContext.getUserId(), dto));
        return dto;
    }

    OrderResponseDto getOrderStatusById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponseDto.fromOrder(order);
    }

    List<OrderResponseDto> getOrdersByLocation() {
        return getOrdersByLocation(null);
    }

    List<OrderResponseDto> getOrdersByLocation(Status statusFilter) {
        Long locationId = UserContext.getLocationId();
        List<Order> orders;
        if (statusFilter != null) {
            orders = orderRepository.findByLocationIdAndStatusIn(locationId, List.of(statusFilter));
        } else {
            orders = orderRepository.findByLocationId(locationId);
        }
        return orders.stream()
                .map(OrderResponseDto::fromOrder)
                .toList();
    }

    @Transactional
    OrderResponseDto updateOrderStatus(UUID orderId, UpdateStatusRequestDto request) {
        for (int attempt = 0; ; attempt++) {
            try {
                return tryUpdateStatus(orderId, request);
            } catch (OptimisticLockingFailureException e) {
                if (attempt >= 3) throw e;
            }
        }
    }

    private OrderResponseDto tryUpdateStatus(UUID orderId, UpdateStatusRequestDto request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Long locationId = UserContext.getLocationId();
        if (!locationId.equals(order.getLocationId())) {
            throw new OrderAccessDeniedException(orderId);
        }

        Status targetStatus;
        try {
            targetStatus = Status.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderException("Invalid status value: " + request.status());
        }

        StatusTransition.validateTransition(order.getStatus(), targetStatus);
        order.updateStatus(targetStatus);
        orderRepository.save(order);
        OrderResponseDto dto = OrderResponseDto.fromOrder(order);
        eventPublisher.publishEvent(new OrderEvent("ORDER_STATUS_CHANGED", order.getLocationId(), UserContext.getUserId(), dto));
        return dto;
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
        for (OrderItemRequestDto item : items) {
            if (item.quantity() < 1) {
                throw new InvalidOrderException(
                        "Each item must have a quantity of at least 1");
            }
        }
        int totalQuantity = items.stream()
                .mapToInt(OrderItemRequestDto::quantity)
                .sum();
        if (totalQuantity < MIN_PIZZAS_PER_ORDER) {
            throw new InvalidOrderException(
                    "You cannot order less than " + MIN_PIZZAS_PER_ORDER + " pizzas per order");
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
        if (phoneNumber.startsWith("+")) {
            return "+" + phoneNumber.substring(1).replaceAll("[^\\d]", "");
        }
        return phoneNumber.replaceAll("[^\\d]", "");
    }

}
