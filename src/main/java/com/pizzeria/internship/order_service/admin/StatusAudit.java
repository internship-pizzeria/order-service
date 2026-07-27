package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.order.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an audit log entry for order status transitions.
 * Used for tracking lifecycle changes, performance analytics, and compliance.
 */
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
    private Long changedBy; // ID of the user or system component that triggered the change

    @Column(nullable = false)
    private Long locationId; // Restaurant location context for filtering analytics

    @CreationTimestamp
    private Instant changedAt;
}