package com.pizzeria.internship.order_service.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="order_item")
public class OrderItem {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long order_id;

    @Column(nullable = false)
    private Integer product_id;

    @Column(nullable = false)
    private Integer quantity;
    private String historical_name;
    private Double historical_price;
}
