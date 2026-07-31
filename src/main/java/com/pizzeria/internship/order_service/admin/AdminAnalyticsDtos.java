package com.pizzeria.internship.order_service.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Temporary holder interface for Admin Analytics Data Transfer Objects (DTOs).
 * These records will be extracted into separate files as individual tasks are implemented.
 */
public interface AdminAnalyticsDtos {



    // Task 4: Peak Hours Analysis
    @Schema(description = "Aggregated statistics for a single hour of the day")
    record PeakHourItem(
            @Schema(description = "Hour of the day (0-23)", example = "19")
            int hour,

            @Schema(description = "Number of orders placed during this hour", example = "42")
            long orderCount,

            @Schema(description = "Revenue generated during this hour", example = "1350.75")
            BigDecimal revenue,

            @Schema(description = "True when the order count for this hour exceeds the configured peak threshold", example = "true")
            boolean isPeak
    ) {}

    @Schema(description = "Hour-by-hour order statistics for the selected time range")
    record PeakHoursResponse(
            @Schema(description = "One entry per hour of the day (always 24 entries)")
            List<PeakHourItem> hours
    ) {}

    // Task 5: Daily/Weekly Trends
    @Schema(description = "Aggregated order statistics for a single time bucket")
    record TrendDataPoint(
            @Schema(description = "Start of the time bucket, formatted according to the requested granularity", example = "2026-07-15")
            String dateOrTime,

            @Schema(description = "Number of orders placed in the bucket", example = "87")
            long orderCount,

            @Schema(description = "Revenue generated in the bucket", example = "4230.00")
            BigDecimal revenue
    ) {}

    @Schema(description = "Time-series trend data for the selected time range")
    record TrendResponse(
            @Schema(description = "Ordered list of time buckets")
            List<TrendDataPoint> trends
    ) {}

}
