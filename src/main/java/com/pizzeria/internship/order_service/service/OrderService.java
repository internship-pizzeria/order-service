package com.pizzeria.internship.order_service.service;

import com.pizzeria.internship.order_service.dto.OrderRequest;
import com.pizzeria.internship.order_service.entity.Order;
import com.pizzeria.internship.order_service.entity.OrderItem;
import com.pizzeria.internship.order_service.entity.Product;
import com.pizzeria.internship.order_service.repository.OrderRepository;
import com.pizzeria.internship.order_service.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    OrderService(OrderRepository orderRepository, ProductRepository productRepository){
        this.orderRepository=orderRepository;
        this.productRepository = productRepository;
    }


    public Order createOrder(OrderRequest request){
        Order order = new Order();
        order.setCustomer_name(request.getCustomerName());
        order.setPhone_number(request.getPhoneNumber());
        order.setDelivery_address(request.getDeliveryAddress());
        order.setTotal_price(request.getTotalPrice());

        for (Long productID : request.getProductIds()) {

            Optional<OrderItem> existingItem = order.getItems().stream()
                    .filter(orderItem ->orderItem.getProduct().getId().equals(productID))
                    .findFirst();

           if(existingItem.isPresent()){
               existingItem.get().setQuantity(existingItem.get().getQuantity()+1);
           }
           else{
               Product product = productRepository.findById(productID)
                       .orElseThrow(() -> new RuntimeException("Product not found: " + productID));
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(1);
                orderItem.setOrder(order);
                orderItem.setHistorical_name(product.getName());
                orderItem.setHistorical_price(product.getPrice());
                order.addItem(orderItem);
           }
        }
        return orderRepository.save(order);
    }

}
