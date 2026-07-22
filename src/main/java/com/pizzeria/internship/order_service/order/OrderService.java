package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.product.Product;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.user.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class OrderService {
    private static final int MIN_PIZZAS_PER_ORDER = 1;
    private static final int MAX_PIZZAS_PER_ORDER = 50;
    private static final String PHONE_NUMBER_REGEX = "^[+\\d\\s\\-()]+$";

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventHandler eventHandler;

    @Transactional
    OrderResponseDto createOrder(OrderRequestDto request) {
        validateRequest(request);
        Order order = buildOrderFromRequest(request);
        request.items().forEach(item -> addItem(order, item.productId(), item.quantity()));
        order.calculateTotalPrice();
        orderRepository.save(order);
        OrderResponseDto dto = OrderResponseDto.fromOrder(order);
        Long locationId = order.getLocationId();
        afterCommit(() -> eventHandler.sendOrderNew(locationId, dto));
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
        orderRepository.updateStatus(orderId, targetStatus);
        OrderResponseDto dto = new OrderResponseDto(
                orderId,
                targetStatus.name(),
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponseDto::fromOrderItem)
                        .toList()
        );
        Long statusLocationId = order.getLocationId();
        afterCommit(() -> eventHandler.sendStatusChanged(statusLocationId, dto));
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

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
