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

    public RevenueResult calculateRevenue(Long locationId, Instant from, Instant to) {
        String whereClause;
        Object[] params;
        if (locationId != null) {
            whereClause = "WHERE created_at >= ? AND created_at < ? AND location_id = ?";
            params = new Object[]{Timestamp.from(from), Timestamp.from(to), locationId};
        } else {
            whereClause = "WHERE created_at >= ? AND created_at < ?";
            params = new Object[]{Timestamp.from(from), Timestamp.from(to)};
        }

        var row = analyticsJdbcTemplate.queryForMap(
                "SELECT COALESCE(SUM(total_price), 0) AS total_revenue, " +
                "COUNT(DISTINCT order_id) AS order_count " +
                "FROM report_order_items " + whereClause, params);

        BigDecimal totalRevenue = (BigDecimal) row.get("total_revenue");
        long orderCount = ((Number) row.get("order_count")).longValue();

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (orderCount > 0) {
            averageOrderValue = totalRevenue.divide(
                    BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        }

        return new RevenueResult(totalRevenue, orderCount, averageOrderValue);
    }

    public record RevenueResult(
            BigDecimal totalRevenue,
            long orderCount,
            BigDecimal averageOrderValue
    ) {}
}
