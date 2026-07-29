package com.pizzeria.internship.order_service.analytics.fulfillment;

import java.util.List;

public record FulfillmentMetricsResponse(
        double averageTimeToDeliverMinutes,
        double medianTimeToDeliverMinutes,
        double p95TimeToDeliverMinutes,
        List<StatusTiming> averageTimePerStatus
) {

    public record StatusTiming(
            String fromStatus,
            String toStatus,
            double averageMinutes,
            double medianMinutes,
            double p95Minutes
    ) {}

    record DeliverySummary(
            double averageMinutes,
            double medianMinutes,
            double p95Minutes
    ) {}
}
