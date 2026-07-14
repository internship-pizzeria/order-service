package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.orderitem.OrderItem;
import com.pizzeria.internship.order_service.product.ProductClient;
import com.pizzeria.internship.order_service.product.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public Order createOrder(OrderRequestDto request) {
        Order order = new Order();
        order.setCustomer_name(request.customerName());
        order.setPhone_number(request.phoneNumber());
        order.setDelivery_address(request.deliveryAddress());
        order.setTotal_price(request.totalPrice());

        for (Long productID : request.productIds()) {

            Optional<OrderItem> existingItem = order.getItems().stream()
                    .filter(orderItem -> orderItem.getProductId().equals(productID))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                ProductDto product = productClient.getProductById(productID);
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.id());
                orderItem.setQuantity(1);
                orderItem.setOrder(order);
                orderItem.setHistorical_name(product.name());
                orderItem.setHistorical_price(product.price());
                order.addItem(orderItem);
            }
        }
        return orderRepository.save(order);
    }
}
