package com.pizzeria.internship.order_service.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "Orders", indexes = {
        @Index(name = "idx_order_location", columnList = "location_id"),
        @Index(name = "idx_order_location_status", columnList = "location_id, status"),
        @Index(name = "idx_order_status_created", columnList = "status, created_at")
})
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private Long locationId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column
    private String rejectionReason;

    @CreationTimestamp
    private Instant createdAt;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream()
                .map(item -> item.getHistoricalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void reject(String reason) {
        this.status = Status.REJECTED;
        this.rejectionReason = reason;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
