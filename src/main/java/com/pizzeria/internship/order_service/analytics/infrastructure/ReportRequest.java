package com.pizzeria.internship.order_service.analytics.infrastructure;

import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import java.time.Instant;

public record ReportRequest(
        AnalyticsScope scope,
        Instant from,
        Instant to
) { }
