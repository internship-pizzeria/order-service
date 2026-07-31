package com.pizzeria.internship.order_service.analytics.infrastructure;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of a generated report")
public enum ReportType {
    REVENUE,
    POPULARITY,
    FULFILLMENT,
    LOCATION_PERFORMANCE,
    PEAK_HOURS
}
