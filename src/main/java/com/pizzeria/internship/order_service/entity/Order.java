package com.pizzeria.internship.order_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;


enum Status{
    NEW,
    ACCEPTED,
    REJECTED,
    PAID
}

@Data
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String customer_name;

    @Column(nullable = false)
    private String phone_number;

    @Column(nullable = false)
    private String delivery_address;

    @Column(nullable = false)
    @Enumerated
    private Enum<Status> status;

    @Column(nullable=false)
    private Double total_price;

    @CreationTimestamp
    private LocalDateTime created_at;
}
