package com.pizzeria.internship.order_service.analytics.peakhours;

import com.pizzeria.internship.order_service.admin.AdminAnalyticsDtos;
import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeakHoursServiceTest {

    @Mock
    private JdbcTemplate analyticsJdbcTemplate;

    private PeakHoursService service;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");

    @BeforeEach
    void setUp() {
        service = new PeakHoursService(analyticsJdbcTemplate);
        ReflectionTestUtils.setField(service, "threshold", 1.5);
    }

    @Test
    void shouldReturnAllHoursWithZerosWhenNoData() {
        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of());

        AdminAnalyticsDtos.PeakHoursResponse response = service.getPeakHours(
                new AllLocations(), FROM, TO);

        assertEquals(24, response.hours().size());
        for (var item : response.hours()) {
            assertEquals(0, item.orderCount());
            assertEquals(BigDecimal.ZERO, item.revenue());
            assertFalse(item.isPeak());
        }
    }

    @Test
    void shouldDetectNoPeaksWhenAllHoursEqual() {
        List<Map<String, Object>> rows = List.of(
                Map.of("hour", 8, "order_count", 5L, "revenue", BigDecimal.valueOf(100)),
                Map.of("hour", 12, "order_count", 5L, "revenue", BigDecimal.valueOf(100))
        );

        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(rows);

        AdminAnalyticsDtos.PeakHoursResponse response = service.getPeakHours(
                new AllLocations(), FROM, TO);

        assertFalse(response.hours().get(8).isPeak());
        assertFalse(response.hours().get(12).isPeak());
    }

    @Test
    void shouldDetectPeakHourWhenSignificantlyHigher() {
        List<Map<String, Object>> rows = List.of(
                Map.of("hour", 8, "order_count", 2L, "revenue", BigDecimal.valueOf(40)),
                Map.of("hour", 12, "order_count", 20L, "revenue", BigDecimal.valueOf(400))
        );

        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(rows);

        AdminAnalyticsDtos.PeakHoursResponse response = service.getPeakHours(
                new AllLocations(), FROM, TO);

        assertFalse(response.hours().get(8).isPeak());
        assertTrue(response.hours().get(12).isPeak());
    }

    @Test
    void shouldFilterByLocation() {
        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(
                        Map.of("hour", 10, "order_count", 3L, "revenue", BigDecimal.valueOf(60))
                ));

        AdminAnalyticsDtos.PeakHoursResponse response = service.getPeakHours(
                new SingleLocation(5L), FROM, TO);

        assertEquals(3L, response.hours().get(10).orderCount());
    }

    @Test
    void shouldUseCustomThreshold() {
        PeakHoursService lowThresholdService = new PeakHoursService(analyticsJdbcTemplate);
        ReflectionTestUtils.setField(lowThresholdService, "threshold", 1.1);

        List<Map<String, Object>> rows = List.of(
                Map.of("hour", 8, "order_count", 2L, "revenue", BigDecimal.valueOf(40)),
                Map.of("hour", 12, "order_count", 10L, "revenue", BigDecimal.valueOf(200))
        );

        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(rows);

        AdminAnalyticsDtos.PeakHoursResponse response = lowThresholdService.getPeakHours(
                new AllLocations(), FROM, TO);

        assertTrue(response.hours().get(12).isPeak());
        assertEquals(10L, response.hours().get(12).orderCount());
    }

    @Test
    void shouldPassCorrectSqlParams() {
        when(analyticsJdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(
                        Map.of("hour", 14, "order_count", 7L, "revenue", BigDecimal.valueOf(140))
                ));

        AdminAnalyticsDtos.PeakHoursResponse response = service.getPeakHours(
                new AllLocations(), FROM, TO);

        assertEquals(7L, response.hours().get(14).orderCount());
        assertEquals(BigDecimal.valueOf(140), response.hours().get(14).revenue());
    }
}
