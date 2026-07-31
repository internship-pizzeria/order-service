package com.pizzeria.internship.order_service.analytics.fulfillment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Fulfillment performance metrics for the selected time range and location scope")
public record FulfillmentMetricsResponse(
        @Schema(description = "Average time between order creation and delivery, in minutes", example = "34.5")
        double averageTimeToDeliverMinutes,

        @Schema(description = "Median time between order creation and delivery, in minutes", example = "31.2")
        double medianTimeToDeliverMinutes,

        @Schema(description = "95th percentile of the time between order creation and delivery, in minutes", example = "52.8")
        double p95TimeToDeliverMinutes,

        @Schema(description = "Average time spent on each status transition")
        List<StatusTiming> averageTimePerStatus
) {

    @Schema(description = "Average duration of a single status transition")
    public record StatusTiming(
            @Schema(description = "Source status of the transition", example = "NEW")
            String fromStatus,

            @Schema(description = "Target status of the transition", example = "ACCEPTED")
            String toStatus,

            @Schema(description = "Average duration of the transition, in minutes", example = "3.2")
            double averageMinutes,

            @Schema(description = "Median duration of the transition, in minutes", example = "2.8")
            double medianMinutes,

            @Schema(description = "95th percentile duration of the transition, in minutes", example = "7.1")
            double p95Minutes
    ) {}

    record DeliverySummary(
            double averageMinutes,
            double medianMinutes,
            double p95Minutes
    ) {}
}
