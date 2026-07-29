package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.AllLocations;
import com.pizzeria.internship.order_service.analytics.AnalyticsScope;
import com.pizzeria.internship.order_service.analytics.OrderAnalyticsFacade;
import com.pizzeria.internship.order_service.analytics.ProductRankingService;
import com.pizzeria.internship.order_service.analytics.SingleLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/analytics")
class AdminAnalyticsController {

    private final OrderAnalyticsFacade orderAnalyticsFacade;
    private final ProductRankingService productRankingService;

    @GetMapping("/revenue")
    public RevenueSummaryResponse getRevenueSummary(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {

        AnalyticsScope scope = resolveScope(locationId);
        OrderAnalyticsFacade.RevenueResult result = orderAnalyticsFacade.calculateRevenue(scope, from, to);

        return new RevenueSummaryResponse(
                result.totalRevenue(),
                result.orderCount(),
                result.averageOrderValue()
        );
    }

    @GetMapping("/products/ranking")
    public ProductRankingResponse getProductRanking(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "BY_REVENUE") RankingSort sortBy,
            @PageableDefault(size = 20) Pageable pageable) {
        AnalyticsScope scope = resolveScope(locationId);
        return productRankingService.getProductRanking(scope, from, to, sortBy, pageable);
    }

    @GetMapping("/locations")
    public AdminAnalyticsDtos.LocationPerformancePageResponse getLocationPerformance(
            @RequestParam(defaultValue = "REVENUE") String metric,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        // TODO (Task 8): Aggregate location metrics, paginate, then fetch city names via batch HTTP call
        return null;
    }

    @GetMapping("/peak-hours")
    public AdminAnalyticsDtos.PeakHoursResponse getPeakHours(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 4): Group orders by extracted hour from createdAt timestamp
        return null;
    }

    @GetMapping("/trends")
    public AdminAnalyticsDtos.TrendResponse getTrends(
            @RequestParam(required = false) Long locationId,
            @RequestParam String granularity,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 5): Implement time-series grouping and date truncation queries
        return null;
    }

    @GetMapping("/fulfillment")
    public AdminAnalyticsDtos.FulfillmentMetricsResponse getFulfillmentMetrics(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 6): Calculate duration differences between NEW and DELIVERED statuses from StatusAudit
        return null;
    }

    static AnalyticsScope resolveScope(Long locationId) {
        return locationId != null ? new SingleLocation(locationId) : new AllLocations();
    }
}