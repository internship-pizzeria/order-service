package com.pizzeria.internship.order_service.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
