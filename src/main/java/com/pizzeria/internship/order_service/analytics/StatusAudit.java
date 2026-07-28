package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.order.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "status_audit")
public class StatusAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status toStatus;

    @Column(nullable = false)
    private Long changedBy;

    @Column(nullable = false)
    private Long locationId;

    @CreationTimestamp
    private Instant changedAt;
}
