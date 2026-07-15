package com.pizzeria.internship.order_service.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_item")

class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Builder.Default
    private Integer quantity = 1;

    private String historicalName;

    private BigDecimal historicalPrice;

    public void setOrder(Order order) {
        this.order = order;
    }
}
