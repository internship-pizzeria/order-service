package com.pizzeria.internship.order_service.analytics.fulfillment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

    @Mock
    private JdbcTemplate analyticsJdbcTemplate;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");

    @Test
    void shouldCalculateDeliveryMetricsWithLocationFilter() {
        when(analyticsJdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("sa_del.to_status = 'DELIVERED'")),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO)), eq(5L)))
                .thenReturn(Map.of(
                        "avg_minutes", 45.5,
                        "median_minutes", 40.0,
                        "p95_minutes", 90.0
                ));

        when(analyticsJdbcTemplate.query(
                argThat(sql -> sql.contains("LAG(changed_at)")),
                any(RowMapper.class),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO)), eq(5L)))
                .thenReturn(List.of(
                        new FulfillmentMetricsResponse.StatusTiming("NEW", "ACCEPTED", 5.0, 4.5, 10.0),
                        new FulfillmentMetricsResponse.StatusTiming("ACCEPTED", "IN_PROGRESS", 10.0, 9.0, 20.0)
                ));

        FulfillmentMetricsResponse response = fulfillmentService.calculateMetrics(5L, FROM, TO);

        assertEquals(45.5, response.averageTimeToDeliverMinutes(), 0.01);
        assertEquals(40.0, response.medianTimeToDeliverMinutes(), 0.01);
        assertEquals(90.0, response.p95TimeToDeliverMinutes(), 0.01);
        assertEquals(2, response.averageTimePerStatus().size());
        assertEquals("NEW", response.averageTimePerStatus().get(0).fromStatus());
        assertEquals("ACCEPTED", response.averageTimePerStatus().get(0).toStatus());
    }

    @Test
    void shouldCalculateDeliveryMetricsWithoutLocationFilter() {
        when(analyticsJdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("sa_del.to_status = 'DELIVERED'")),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(Map.of(
                        "avg_minutes", 50.0,
                        "median_minutes", 45.0,
                        "p95_minutes", 95.0
                ));

        when(analyticsJdbcTemplate.query(
                argThat(sql -> sql.contains("LAG(changed_at)")),
                any(RowMapper.class),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(List.of());

        FulfillmentMetricsResponse response = fulfillmentService.calculateMetrics(null, FROM, TO);

        assertEquals(50.0, response.averageTimeToDeliverMinutes(), 0.01);
        assertEquals(45.0, response.medianTimeToDeliverMinutes(), 0.01);
        assertEquals(95.0, response.p95TimeToDeliverMinutes(), 0.01);
        assertTrue(response.averageTimePerStatus().isEmpty());
    }

    @Test
    void shouldReturnZeroWhenNoDeliveredOrders() {
        when(analyticsJdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("sa_del.to_status = 'DELIVERED'")),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(Map.of(
                        "avg_minutes", 0.0,
                        "median_minutes", 0.0,
                        "p95_minutes", 0.0
                ));

        when(analyticsJdbcTemplate.query(
                argThat(sql -> sql.contains("LAG(changed_at)")),
                any(RowMapper.class),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(List.of());

        FulfillmentMetricsResponse response = fulfillmentService.calculateMetrics(null, FROM, TO);

        assertEquals(0.0, response.averageTimeToDeliverMinutes(), 0.01);
        assertEquals(0.0, response.medianTimeToDeliverMinutes(), 0.01);
        assertEquals(0.0, response.p95TimeToDeliverMinutes(), 0.01);
    }

    @Test
    void shouldReturnAllStatusTransitions() {
        when(analyticsJdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("sa_del.to_status = 'DELIVERED'")),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO)), eq(3L)))
                .thenReturn(Map.of(
                        "avg_minutes", 60.0,
                        "median_minutes", 55.0,
                        "p95_minutes", 110.0
                ));

        List<FulfillmentMetricsResponse.StatusTiming> timings = List.of(
                new FulfillmentMetricsResponse.StatusTiming("NEW", "ACCEPTED", 2.0, 1.5, 5.0),
                new FulfillmentMetricsResponse.StatusTiming("ACCEPTED", "IN_PROGRESS", 8.0, 7.0, 15.0),
                new FulfillmentMetricsResponse.StatusTiming("IN_PROGRESS", "READY", 12.0, 11.0, 22.0),
                new FulfillmentMetricsResponse.StatusTiming("READY", "PAID", 3.0, 2.5, 6.0),
                new FulfillmentMetricsResponse.StatusTiming("PAID", "IN_DELIVERY", 5.0, 4.0, 10.0),
                new FulfillmentMetricsResponse.StatusTiming("IN_DELIVERY", "DELIVERED", 15.0, 14.0, 28.0)
        );

        when(analyticsJdbcTemplate.query(
                argThat(sql -> sql.contains("LAG(changed_at)")),
                any(RowMapper.class),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO)), eq(3L)))
                .thenReturn(timings);

        FulfillmentMetricsResponse response = fulfillmentService.calculateMetrics(3L, FROM, TO);

        assertEquals(6, response.averageTimePerStatus().size());
        assertEquals("NEW", response.averageTimePerStatus().get(0).fromStatus());
        assertEquals("DELIVERED", response.averageTimePerStatus().get(5).toStatus());
        assertTrue(response.averageTimePerStatus().stream()
                .allMatch(t -> t.averageMinutes() > 0));
    }

    @Test
    void shouldFilterOutInvalidTransitions() {
        when(analyticsJdbcTemplate.queryForMap(
                argThat(sql -> sql.contains("sa_del.to_status = 'DELIVERED'")),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(Map.of(
                        "avg_minutes", 60.0,
                        "median_minutes", 55.0,
                        "p95_minutes", 110.0
                ));

        List<FulfillmentMetricsResponse.StatusTiming> timings = List.of(
                new FulfillmentMetricsResponse.StatusTiming("NEW", "ACCEPTED", 2.0, 1.5, 5.0),
                new FulfillmentMetricsResponse.StatusTiming("NEW", "DELIVERED", 30.0, 28.0, 50.0),
                new FulfillmentMetricsResponse.StatusTiming("ACCEPTED", "IN_PROGRESS", 8.0, 7.0, 15.0),
                new FulfillmentMetricsResponse.StatusTiming("DELIVERED", "READY", 1.0, 0.5, 2.0)
        );

        when(analyticsJdbcTemplate.query(
                argThat(sql -> sql.contains("LAG(changed_at)")),
                any(RowMapper.class),
                eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(timings);

        FulfillmentMetricsResponse response = fulfillmentService.calculateMetrics(null, FROM, TO);

        assertEquals(2, response.averageTimePerStatus().size());
        assertEquals("NEW", response.averageTimePerStatus().get(0).fromStatus());
        assertEquals("ACCEPTED", response.averageTimePerStatus().get(0).toStatus());
        assertEquals("ACCEPTED", response.averageTimePerStatus().get(1).fromStatus());
        assertEquals("IN_PROGRESS", response.averageTimePerStatus().get(1).toStatus());
    }
}
