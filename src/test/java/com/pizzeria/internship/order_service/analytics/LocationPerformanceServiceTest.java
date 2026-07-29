package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.admin.LocationMetrics;
import com.pizzeria.internship.order_service.admin.LocationPerformancePageResponse;
import com.pizzeria.internship.order_service.location.LocationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationPerformanceServiceTest {

    @Mock
    private JdbcTemplate analyticsJdbcTemplate;

    @Mock
    private LocationClient locationClient;

    @InjectMocks
    private LocationPerformanceService service;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");
    private static final Timestamp FROM_TS = Timestamp.from(FROM);
    private static final Timestamp TO_TS = Timestamp.from(TO);
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Test
    void shouldReturnEmptyPageWhenNoData() {
        // GIVEN
        when(analyticsJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), eq(FROM_TS), eq(TO_TS)))
                .thenReturn(0L);

        // WHEN
        LocationPerformancePageResponse response =
                service.getLocationPerformance("REVENUE", FROM, TO, PAGEABLE);

        // THEN
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        verify(locationClient, never()).getCityNameMap(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnLocationsWithRevenueSort() {
        // GIVEN
        when(analyticsJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), eq(FROM_TS), eq(TO_TS)))
                .thenReturn(2L);

        ResultSet rs = mock(ResultSet.class);
        when(analyticsJdbcTemplate.query(
                anyString(), any(RowMapper.class), eq(FROM_TS), eq(TO_TS), anyInt(), anyLong()))
                .thenAnswer(invocation -> {
                    RowMapper<LocationMetrics> mapper = invocation.getArgument(1);
                    when(rs.getLong("location_id")).thenReturn(1L, 2L);
                    when(rs.getBigDecimal("total_revenue")).thenReturn(
                            new BigDecimal("500.00"), new BigDecimal("300.00"));
                    when(rs.getLong("order_count")).thenReturn(10L, 5L);
                    return List.of(
                            mapper.mapRow(rs, 0),
                            mapper.mapRow(rs, 1)
                    );
                });

        when(locationClient.getCityNameMap(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, "Warszawa", 2L, "Kraków"));

        // WHEN
        LocationPerformancePageResponse response =
                service.getLocationPerformance("REVENUE", FROM, TO, PAGEABLE);

        // THEN
        assertEquals(2, response.content().size());
        assertEquals("Warszawa", response.content().get(0).cityName());
        assertEquals(new BigDecimal("500.00"), response.content().get(0).totalRevenue());
        assertEquals(10L, response.content().get(0).orderCount());
        assertEquals("Kraków", response.content().get(1).cityName());
        assertEquals(new BigDecimal("300.00"), response.content().get(1).totalRevenue());
        assertEquals(5L, response.content().get(1).orderCount());
        assertEquals(0, response.pageNumber());
        assertEquals(20, response.pageSize());
        assertEquals(2, response.totalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSortByOrderCountWhenSpecified() {
        // GIVEN
        when(analyticsJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), eq(FROM_TS), eq(TO_TS)))
                .thenReturn(2L);

        ResultSet rs = mock(ResultSet.class);
        when(analyticsJdbcTemplate.query(
                anyString(), any(RowMapper.class), eq(FROM_TS), eq(TO_TS), anyInt(), anyLong()))
                .thenAnswer(invocation -> {
                    RowMapper<LocationMetrics> mapper = invocation.getArgument(1);
                    when(rs.getLong("location_id")).thenReturn(3L, 4L);
                    when(rs.getBigDecimal("total_revenue")).thenReturn(
                            new BigDecimal("200.00"), new BigDecimal("400.00"));
                    when(rs.getLong("order_count")).thenReturn(50L, 20L);
                    return List.of(
                            mapper.mapRow(rs, 0),
                            mapper.mapRow(rs, 1)
                    );
                });

        // WHEN
        LocationPerformancePageResponse response =
                service.getLocationPerformance("ORDER_COUNT", FROM, TO, PAGEABLE);

        // THEN
        assertEquals(2, response.content().size());
        assertEquals(50L, response.content().get(0).orderCount());
        assertEquals(20L, response.content().get(1).orderCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldFallbackToUnknownWhenCityNotFound() {
        // GIVEN
        when(analyticsJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), eq(FROM_TS), eq(TO_TS)))
                .thenReturn(1L);

        ResultSet rs = mock(ResultSet.class);
        when(analyticsJdbcTemplate.query(
                anyString(), any(RowMapper.class), eq(FROM_TS), eq(TO_TS), anyInt(), anyLong()))
                .thenAnswer(invocation -> {
                    RowMapper<LocationMetrics> mapper = invocation.getArgument(1);
                    when(rs.getLong("location_id")).thenReturn(99L);
                    when(rs.getBigDecimal("total_revenue")).thenReturn(new BigDecimal("100.00"));
                    when(rs.getLong("order_count")).thenReturn(3L);
                    return List.of(mapper.mapRow(rs, 0));
                });

        when(locationClient.getCityNameMap(List.of(99L)))
                .thenReturn(Map.of());

        // WHEN
        LocationPerformancePageResponse response =
                service.getLocationPerformance("REVENUE", FROM, TO, PAGEABLE);

        // THEN
        assertEquals(1, response.content().size());
        assertEquals("Unknown", response.content().getFirst().cityName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseCorrectPaginationParams() {
        // GIVEN
        Pageable pageable = PageRequest.of(2, 10);

        when(analyticsJdbcTemplate.queryForObject(
                anyString(), eq(Long.class), eq(FROM_TS), eq(TO_TS)))
                .thenReturn(100L);

        when(analyticsJdbcTemplate.query(
                anyString(), any(RowMapper.class), eq(FROM_TS), eq(TO_TS),
                anyInt(), anyLong()))
                .thenReturn(List.of());

        // WHEN
        service.getLocationPerformance("REVENUE", FROM, TO, pageable);

        // THEN
        verify(analyticsJdbcTemplate).query(
                anyString(), any(RowMapper.class), eq(FROM_TS), eq(TO_TS),
                eq(10), eq(20L));
    }
}
