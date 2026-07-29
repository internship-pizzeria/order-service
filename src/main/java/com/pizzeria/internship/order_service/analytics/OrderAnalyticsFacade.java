package com.pizzeria.internship.order_service.analytics;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;

@Service
public class OrderAnalyticsFacade {

    private final JdbcTemplate analyticsJdbcTemplate;

    public OrderAnalyticsFacade(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    public RevenueResult calculateRevenue(AnalyticsScope scope, Instant from, Instant to) {
        Object[] params = concat(
                new Object[]{Timestamp.from(from), Timestamp.from(to)},
                scope.sqlParams()
        );

        var row = analyticsJdbcTemplate.queryForMap(
                "SELECT COALESCE(SUM(total_price), 0) AS total_revenue, " +
                "COUNT(DISTINCT order_id) AS order_count " +
                "FROM report_order_items WHERE created_at >= ? AND created_at < ? " +
                scope.sqlSuffix(), params);

        BigDecimal totalRevenue = (BigDecimal) row.get("total_revenue");
        long orderCount = ((Number) row.get("order_count")).longValue();

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (orderCount > 0) {
            averageOrderValue = totalRevenue.divide(
                    BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        }

        return new RevenueResult(totalRevenue, orderCount, averageOrderValue);
    }

    private static Object[] concat(Object[] a, Object[] b) {
        Object[] result = new Object[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public record RevenueResult(
            BigDecimal totalRevenue,
            long orderCount,
            BigDecimal averageOrderValue
    ) {}
}
