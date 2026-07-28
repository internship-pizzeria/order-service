package com.pizzeria.internship.order_service.analytics;

import com.pizzeria.internship.order_service.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyAnalyticsScheduler {

    private final OrderRepository orderRepository;
    private final DailyLocationStatsRepository statsRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void aggregatePreviousDayStats() {
        LocalDate targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        log.info("Starting daily analytics aggregation for date: {}", targetDate);

        Instant dayStart = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<OrderRepository.DailyAggregation> aggregations =
                orderRepository.getDailyAggregations(dayStart, dayEnd);

        if (aggregations.isEmpty()) {
            log.info("No orders found for date: {}", targetDate);
            return;
        }

        for (OrderRepository.DailyAggregation agg : aggregations) {
            statsRepository.upsertStats(
                    targetDate,
                    agg.getLocationId(),
                    agg.getTotalRevenue(),
                    agg.getOrderCount()
            );
        }

        log.info("Aggregated {} locations for date {}", aggregations.size(), targetDate);
    }
}
