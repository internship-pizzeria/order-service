package com.pizzeria.internship.order_service.analytics.fulfillment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class FulfillmentService {

    private final JdbcTemplate jdbcTemplate;

    public FulfillmentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public FulfillmentMetricsResponse calculateMetrics(Long locationId, Instant from, Instant to) {
        Timestamp fromTs = Timestamp.from(from);
        Timestamp toTs = Timestamp.from(to);

        FulfillmentMetricsResponse.DeliverySummary delivery = getDeliverySummary(locationId, fromTs, toTs);
        List<FulfillmentMetricsResponse.StatusTiming> perStatus = getPerStatusTimings(locationId, fromTs, toTs);

        return new FulfillmentMetricsResponse(
                delivery.averageMinutes(),
                delivery.medianMinutes(),
                delivery.p95Minutes(),
                perStatus
        );
    }

    private FulfillmentMetricsResponse.DeliverySummary getDeliverySummary(Long locationId, Timestamp from, Timestamp to) {
        String sql;
        Object[] params;

        if (locationId != null) {
            sql = """
                    WITH delivery_times AS (
                        SELECT EXTRACT(EPOCH FROM (sa_del.changed_at - sa_new.changed_at)) / 60.0 AS duration_minutes
                        FROM status_audit sa_new
                        JOIN status_audit sa_del
                            ON sa_new.order_id = sa_del.order_id
                            AND sa_new.to_status = 'NEW'
                            AND sa_del.to_status = 'DELIVERED'
                            AND sa_del.changed_at > sa_new.changed_at
                        WHERE sa_new.changed_at >= ? AND sa_del.changed_at < ?
                          AND sa_new.location_id = ?
                    )
                    SELECT COALESCE(AVG(duration_minutes), 0) AS avg_minutes,
                           COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_minutes), 0) AS median_minutes,
                           COALESCE(PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_minutes), 0) AS p95_minutes
                    FROM delivery_times
                    """;
            params = new Object[]{from, to, locationId};
        } else {
            sql = """
                    WITH delivery_times AS (
                        SELECT EXTRACT(EPOCH FROM (sa_del.changed_at - sa_new.changed_at)) / 60.0 AS duration_minutes
                        FROM status_audit sa_new
                        JOIN status_audit sa_del
                            ON sa_new.order_id = sa_del.order_id
                            AND sa_new.to_status = 'NEW'
                            AND sa_del.to_status = 'DELIVERED'
                            AND sa_del.changed_at > sa_new.changed_at
                        WHERE sa_new.changed_at >= ? AND sa_del.changed_at < ?
                    )
                    SELECT COALESCE(AVG(duration_minutes), 0) AS avg_minutes,
                           COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_minutes), 0) AS median_minutes,
                           COALESCE(PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_minutes), 0) AS p95_minutes
                    FROM delivery_times
                    """;
            params = new Object[]{from, to};
        }

        var row = jdbcTemplate.queryForMap(sql, params);
        return new FulfillmentMetricsResponse.DeliverySummary(
                ((Number) row.get("avg_minutes")).doubleValue(),
                ((Number) row.get("median_minutes")).doubleValue(),
                ((Number) row.get("p95_minutes")).doubleValue()
        );
    }

    private List<FulfillmentMetricsResponse.StatusTiming> getPerStatusTimings(Long locationId, Timestamp from, Timestamp to) {
        String sql;
        Object[] params;

        if (locationId != null) {
            sql = """
                    WITH ordered_audit AS (
                        SELECT order_id, location_id, from_status, to_status, changed_at,
                               LAG(changed_at) OVER (PARTITION BY order_id ORDER BY changed_at) AS prev_changed_at,
                               LAG(to_status) OVER (PARTITION BY order_id ORDER BY changed_at) AS prev_to_status
                        FROM status_audit
                        WHERE changed_at >= ? AND changed_at < ?
                          AND location_id = ?
                    )
                    SELECT prev_to_status AS from_status,
                           to_status,
                           AVG(duration_minutes) AS avg,
                           PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_minutes) AS median,
                           PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_minutes) AS p95
                    FROM (
                        SELECT prev_to_status, to_status, changed_at,
                               EXTRACT(EPOCH FROM (changed_at - prev_changed_at)) / 60.0 AS duration_minutes
                        FROM ordered_audit
                        WHERE prev_changed_at IS NOT NULL
                          AND to_status != 'REJECTED'
                    ) subq
                    GROUP BY prev_to_status, to_status
                    ORDER BY MIN(changed_at)
                    """;
            params = new Object[]{from, to, locationId};
        } else {
            sql = """
                    WITH ordered_audit AS (
                        SELECT order_id, location_id, from_status, to_status, changed_at,
                               LAG(changed_at) OVER (PARTITION BY order_id ORDER BY changed_at) AS prev_changed_at,
                               LAG(to_status) OVER (PARTITION BY order_id ORDER BY changed_at) AS prev_to_status
                        FROM status_audit
                        WHERE changed_at >= ? AND changed_at < ?
                    )
                    SELECT prev_to_status AS from_status,
                           to_status,
                           AVG(duration_minutes) AS avg,
                           PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY duration_minutes) AS median,
                           PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_minutes) AS p95
                    FROM (
                        SELECT prev_to_status, to_status, changed_at,
                               EXTRACT(EPOCH FROM (changed_at - prev_changed_at)) / 60.0 AS duration_minutes
                        FROM ordered_audit
                        WHERE prev_changed_at IS NOT NULL
                          AND to_status != 'REJECTED'
                    ) subq
                    GROUP BY prev_to_status, to_status
                    ORDER BY MIN(changed_at)
                    """;
            params = new Object[]{from, to};
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Object fromStatus = rs.getObject("from_status");
            return new FulfillmentMetricsResponse.StatusTiming(
                    fromStatus != null ? rs.getString("from_status") : "NEW",
                    rs.getString("to_status"),
                    rs.getDouble("avg"),
                    rs.getDouble("median"),
                    rs.getDouble("p95")
            );
        }, params);
    }
}
