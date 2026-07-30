package com.pizzeria.internship.order_service.analytics.peakhours;

import com.pizzeria.internship.order_service.admin.AdminAnalyticsDtos;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PeakHoursService {

    private final JdbcTemplate analyticsJdbcTemplate;

    @Value("${peak-hours.threshold:1.5}")
    private double threshold;

    public PeakHoursService(
            @Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    public AdminAnalyticsDtos.PeakHoursResponse getPeakHours(AnalyticsScope scope, Instant from, Instant to) {
        Object[] baseParams = new Object[]{Timestamp.from(from), Timestamp.from(to)};
        Object[] params = concat(baseParams, scope.sqlParams());

        String sql = "SELECT EXTRACT(HOUR FROM created_at) AS hour, " +
                "COUNT(*) AS order_count, " +
                "COALESCE(SUM(total_price), 0) AS revenue " +
                "FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " +
                scope.sqlSuffix() + " " +
                "GROUP BY EXTRACT(HOUR FROM created_at) " +
                "ORDER BY hour";

        List<Map<String, Object>> rows = analyticsJdbcTemplate.queryForList(sql, params);

        List<AdminAnalyticsDtos.PeakHourItem> items = new ArrayList<>(24);
        int[] hourData = new int[24];
        BigDecimal[] revenueData = new BigDecimal[24];
        for (int i = 0; i < 24; i++) {
            revenueData[i] = BigDecimal.ZERO;
        }

        long totalOrderCount = 0;
        int hoursWithData = 0;

        for (var row : rows) {
            int hour = ((Number) row.get("hour")).intValue();
            int count = ((Number) row.get("order_count")).intValue();
            BigDecimal rev = (BigDecimal) row.get("revenue");
            hourData[hour] = count;
            revenueData[hour] = rev;
            totalOrderCount += count;
            hoursWithData++;
        }

        double avg = hoursWithData > 0 ? (double) totalOrderCount / hoursWithData : 0;

        for (int hour = 0; hour < 24; hour++) {
            boolean isPeak = hoursWithData > 0 && hourData[hour] > threshold * avg;
            items.add(new AdminAnalyticsDtos.PeakHourItem(
                    hour, hourData[hour], revenueData[hour], isPeak));
        }

        return new AdminAnalyticsDtos.PeakHoursResponse(items);
    }

    private static Object[] concat(Object[] a, Object[] b) {
        Object[] result = new Object[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
