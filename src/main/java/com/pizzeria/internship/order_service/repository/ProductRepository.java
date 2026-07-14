package com.pizzeria.internship.order_service.repository;

import com.pizzeria.internship.order_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
