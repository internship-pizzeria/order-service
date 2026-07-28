package com.pizzeria.internship.order_service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAnalyticsFacadeTest {

    @Mock
    private DailyLocationStatsRepository statsRepository;

    @Mock
    private RevenueCacheService revenueCacheService;

    private static final LocalDate FIXED_TODAY = LocalDate.of(2026, 7, 28);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);

    @InjectMocks
    private OrderAnalyticsFacade orderAnalyticsFacade;

    @BeforeEach
    void setUp() {
        orderAnalyticsFacade = new OrderAnalyticsFacade(statsRepository, revenueCacheService, FIXED_CLOCK);
    }

    @Test
    void shouldCalculateRevenueFromHistoryOnly() {
        Instant from = Instant.parse("2026-07-01T00:00:00Z");
        Instant to = Instant.parse("2026-07-27T23:59:59Z");

        DailyLocationStatsRepository.RevenueAggregation mockAggregation =
                new DailyLocationStatsRepository.RevenueAggregation() {
                    @Override public BigDecimal getTotalRevenue() { return new BigDecimal("150.00"); }
                    @Override public long getOrderCount() { return 3; }
                };

        when(statsRepository.getAggregatedStats(eq(null), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(mockAggregation);

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(null, from, to);

        assertEquals(new BigDecimal("150.00"), result.totalRevenue());
        assertEquals(3, result.orderCount());
        assertEquals(new BigDecimal("50.00"), result.averageOrderValue());
    }

    @Test
    void shouldCalculateRevenueFromRedisOnly() {
        Instant from = Instant.parse("2026-07-28T00:00:00Z");
        Instant to = Instant.parse("2026-07-28T23:59:59Z");

        when(revenueCacheService.getTodayRevenue(1L))
                .thenReturn(new BigDecimal("200.00"));
        when(revenueCacheService.getTodayOrderCount(1L))
                .thenReturn(4L);

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(1L, from, to);

        assertEquals(new BigDecimal("200.00"), result.totalRevenue());
        assertEquals(4, result.orderCount());
        assertEquals(new BigDecimal("50.00"), result.averageOrderValue());
    }

    @Test
    void shouldReturnZeroWhenNoOrders() {
        Instant from = Instant.parse("2026-07-01T00:00:00Z");
        Instant to = Instant.parse("2026-07-27T23:59:59Z");

        DailyLocationStatsRepository.RevenueAggregation emptyAggregation =
                new DailyLocationStatsRepository.RevenueAggregation() {
                    @Override public BigDecimal getTotalRevenue() { return BigDecimal.ZERO; }
                    @Override public long getOrderCount() { return 0; }
                };

        when(statsRepository.getAggregatedStats(eq(null), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(emptyAggregation);

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(null, from, to);

        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0, result.orderCount());
        assertEquals(BigDecimal.ZERO, result.averageOrderValue());
    }

    @Test
    void shouldCalculateRevenueFromBothSources() {
        Instant from = Instant.parse("2026-07-27T00:00:00Z");
        Instant to = Instant.parse("2026-07-28T23:59:59Z");

        DailyLocationStatsRepository.RevenueAggregation historical =
                new DailyLocationStatsRepository.RevenueAggregation() {
                    @Override public BigDecimal getTotalRevenue() { return new BigDecimal("100.00"); }
                    @Override public long getOrderCount() { return 2; }
                };

        when(statsRepository.getAggregatedStats(eq(null), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(historical);
        when(revenueCacheService.getAllLocationsTodayRevenue())
                .thenReturn(Map.of(1L, new BigDecimal("50.00")));
        when(revenueCacheService.getAllLocationsTodayOrderCount())
                .thenReturn(Map.of(1L, 1L));

        OrderAnalyticsFacade.RevenueResult result =
                orderAnalyticsFacade.calculateRevenue(null, from, to);

        assertEquals(new BigDecimal("150.00"), result.totalRevenue());
        assertEquals(3, result.orderCount());
        assertEquals(new BigDecimal("50.00"), result.averageOrderValue());
    }
}
