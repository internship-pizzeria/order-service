package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.orderitem.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private String customer_name;

    @Column(nullable = false)
    private String phone_number;

    @Column(nullable = false)
    private String delivery_address;

    @Enumerated
    private Status status;

    @Column(nullable=false)
    private BigDecimal total_price;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @CreationTimestamp
    private LocalDateTime created_at;
}
