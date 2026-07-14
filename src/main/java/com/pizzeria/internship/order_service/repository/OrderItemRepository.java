package com.pizzeria.internship.order_service.repository;

import com.pizzeria.internship.order_service.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
}
