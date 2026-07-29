package com.pizzeria.internship.order_service.analytics.report;

import java.time.Instant;

public record ReportRequest(
        Long locationId,
        Instant from,
        Instant to
) { }
