package com.pizzeria.internship.order_service.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.Map;

@Service
public class OrderAnalyticsFacade {

    private final DailyLocationStatsRepository statsRepository;
    private final RevenueCacheService revenueCacheService;
    private final Clock clock;

    @Autowired
    public OrderAnalyticsFacade(DailyLocationStatsRepository statsRepository,
                                RevenueCacheService revenueCacheService) {
        this(statsRepository, revenueCacheService, Clock.systemUTC());
    }

    OrderAnalyticsFacade(DailyLocationStatsRepository statsRepository,
                         RevenueCacheService revenueCacheService,
                         Clock clock) {
        this.statsRepository = statsRepository;
        this.revenueCacheService = revenueCacheService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public RevenueResult calculateRevenue(Long locationId, Instant from, Instant to) {
        LocalDate fromDate = from.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate toDate = to.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate today = LocalDate.now(clock);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long orderCount = 0;

        LocalDate historicalTo = today;
        if (fromDate.isBefore(historicalTo)) {
            DailyLocationStatsRepository.RevenueAggregation historical =
                    statsRepository.getAggregatedStats(locationId, fromDate, historicalTo);
            totalRevenue = totalRevenue.add(historical.getTotalRevenue() != null
                    ? historical.getTotalRevenue() : BigDecimal.ZERO);
            orderCount += historical.getOrderCount();
        }

        if (!toDate.isBefore(today)) {
            if (locationId != null) {
                totalRevenue = totalRevenue.add(revenueCacheService.getTodayRevenue(locationId));
                orderCount += revenueCacheService.getTodayOrderCount(locationId);
            } else {
                Map<Long, BigDecimal> allRevenue = revenueCacheService.getAllLocationsTodayRevenue();
                Map<Long, Long> allCounts = revenueCacheService.getAllLocationsTodayOrderCount();
                for (Map.Entry<Long, BigDecimal> entry : allRevenue.entrySet()) {
                    totalRevenue = totalRevenue.add(entry.getValue());
                    Long count = allCounts.get(entry.getKey());
                    orderCount += count != null ? count : 0;
                }
            }
        }

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (orderCount > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        }

        return new RevenueResult(totalRevenue, orderCount, averageOrderValue);
    }

    public record RevenueResult(
            BigDecimal totalRevenue,
            long orderCount,
            BigDecimal averageOrderValue
    ) {}
}
