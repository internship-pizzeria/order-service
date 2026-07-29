package com.pizzeria.internship.order_service.analytics.report;

import com.pizzeria.internship.order_service.analytics.AnalyticsScope;
import java.time.Instant;

public record ReportRequest(
        AnalyticsScope scope,
        Instant from,
        Instant to
) { }
