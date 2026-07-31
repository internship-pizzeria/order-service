package com.pizzeria.internship.order_service.analytics.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a report generation job")
public enum ReportStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}
