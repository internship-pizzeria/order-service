package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.admin.LocationMetrics;
import com.pizzeria.internship.order_service.admin.LocationPerformancePageResponse;
import com.pizzeria.internship.order_service.location.LocationClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class LocationPerformanceService {

    private final JdbcTemplate analyticsJdbcTemplate;
    private final LocationClient locationClient;

    LocationPerformanceService(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate, LocationClient locationClient) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
        this.locationClient = locationClient;
    }

    public LocationPerformancePageResponse getLocationPerformance(
            String metric, Instant from, Instant to, Pageable pageable) {

        Timestamp fromTs = Timestamp.from(from);
        Timestamp toTs = Timestamp.from(to);

        long totalElements = analyticsJdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT location_id) FROM report_order_items " +
                        "WHERE created_at >= ? AND created_at < ?",
                Long.class, fromTs, toTs);

        String orderBy = switch (metric.toUpperCase()) {
            case "ORDER_COUNT" -> "order_count DESC";
            default -> "total_revenue DESC";
        };

        List<LocationMetrics> rows = analyticsJdbcTemplate.query(
                "SELECT location_id, " +
                        "SUM(total_price) AS total_revenue, " +
                        "COUNT(DISTINCT order_id) AS order_count " +
                        "FROM report_order_items " +
                        "WHERE created_at >= ? AND created_at < ? " +
                        "GROUP BY location_id " +
                        "ORDER BY " + orderBy + " " +
                        "LIMIT ? OFFSET ?",
                (rs, rowNum) -> new LocationMetrics(
                        rs.getLong("location_id"),
                        null,
                        rs.getBigDecimal("total_revenue"),
                        rs.getLong("order_count"),
                        0.0
                ),
                fromTs, toTs, pageable.getPageSize(), pageable.getOffset());

        List<Long> ids = rows.stream().map(LocationMetrics::locationId).toList();
        if (!ids.isEmpty()) {
            Map<Long, String> cityNames = locationClient.getCityNameMap(ids);
            rows = rows.stream()
                    .map(m -> new LocationMetrics(
                            m.locationId(),
                            cityNames.getOrDefault(m.locationId(), "Unknown"),
                            m.totalRevenue(),
                            m.orderCount(),
                            m.fulfillmentTimeMinutes()
                    ))
                    .toList();
        }

        return new LocationPerformancePageResponse(
                rows, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }
}