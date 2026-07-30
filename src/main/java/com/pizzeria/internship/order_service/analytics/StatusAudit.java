package com.pizzeria.internship.order_service.analytics;

import java.time.Instant;
import java.util.UUID;

public class StatusAudit {
    private Long id;
    private UUID orderId;
    private String fromStatus;
    private String toStatus;
    private Long changedBy;
    private Long locationId;
    private Instant changedAt;
}
