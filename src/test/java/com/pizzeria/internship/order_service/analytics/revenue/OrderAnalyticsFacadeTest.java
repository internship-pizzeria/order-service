package com.pizzeria.internship.order_service.analytics.revenue;

import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAnalyticsFacadeTest {

    @Mock
    private JdbcTemplate analyticsJdbcTemplate;

    @InjectMocks
    private OrderAnalyticsFacade orderAnalyticsFacade;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");

    @Test
    void shouldCalculateRevenueWithLocationFilter() {
        when(analyticsJdbcTemplate.queryForMap(
                any(String.class), eq(Timestamp.from(FROM)), eq(Timestamp.from(TO)), eq(5L)))
                .thenReturn(Map.of("total_revenue", new BigDecimal("299.90"), "order_count", 3L));

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(new SingleLocation(5L), FROM, TO);

        assertEquals(new BigDecimal("299.90"), result.totalRevenue());
        assertEquals(3, result.orderCount());
        assertEquals(new BigDecimal("99.97"), result.averageOrderValue());
    }

    @Test
    void shouldCalculateRevenueWithoutLocationFilter() {
        when(analyticsJdbcTemplate.queryForMap(
                any(String.class), eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(Map.of("total_revenue", new BigDecimal("1000.00"), "order_count", 10L));

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(new AllLocations(), FROM, TO);

        assertEquals(new BigDecimal("1000.00"), result.totalRevenue());
        assertEquals(10, result.orderCount());
        assertEquals(new BigDecimal("100.00"), result.averageOrderValue());
    }

    @Test
    void shouldReturnZeroWhenNoOrders() {
        when(analyticsJdbcTemplate.queryForMap(
                any(String.class), eq(Timestamp.from(FROM)), eq(Timestamp.from(TO))))
                .thenReturn(Map.of("total_revenue", BigDecimal.ZERO, "order_count", 0L));

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(new AllLocations(), FROM, TO);

        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0, result.orderCount());
        assertEquals(BigDecimal.ZERO, result.averageOrderValue());
    }
}
