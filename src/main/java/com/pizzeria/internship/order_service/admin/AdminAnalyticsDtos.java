package com.pizzeria.internship.order_service.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Temporary holder interface for Admin Analytics Data Transfer Objects (DTOs).
 * These records will be extracted into separate files as individual tasks are implemented.
 */
public interface AdminAnalyticsDtos {



    // Task 4: Peak Hours Analysis
    record PeakHourItem(
            int hour,
            long orderCount,
            BigDecimal revenue,
            boolean isPeak
    ) {}

    record PeakHoursResponse(
            List<PeakHourItem> hours
    ) {}

    // Task 5: Daily/Weekly Trends
    record TrendDataPoint(
            String dateOrTime,
            long orderCount,
            BigDecimal revenue
    ) {}

    record TrendResponse(
            List<TrendDataPoint> trends
    ) {}

}