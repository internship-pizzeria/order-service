package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.OrderAnalyticsFacade;
import com.pizzeria.internship.order_service.analytics.ProductRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

/**
 * REST Controller exposing administrative analytics and reporting endpoints.
 * Secured via Spring Security and restricted to authorized admin roles/users.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/analytics")
class AdminAnalyticsController {

    // =========================================================================
    // PERSON B SECTION: Revenue, Rankings, and Location Performance
    // =========================================================================
    private final OrderAnalyticsFacade orderAnalyticsFacade;
    private final ProductRankingService productRankingService;

    @GetMapping("/revenue")
    public RevenueSummaryResponse getRevenueSummary(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {

        OrderAnalyticsFacade.RevenueResult result = orderAnalyticsFacade.calculateRevenue(locationId, from, to);

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
        return productRankingService.getProductRanking(locationId, from, to, sortBy, pageable);
    }

    /**
     * Task 8: Compares performance metrics across locations with paginated results
     * and batch-fetched city names from catalog-service.
     */
    @GetMapping("/locations")
    public AdminAnalyticsDtos.LocationPerformancePageResponse getLocationPerformance(
            @RequestParam(defaultValue = "REVENUE") String metric,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        // TODO (Task 8): Aggregate location metrics, paginate, then fetch city names via batch HTTP call
        return null;
    }

    // =========================================================================
    // PERSON A SECTION: Order Lifecycle, Timing, and Trends
    // =========================================================================

    /**
     * Task 4: Analyzes order distribution across hours of the day to find peak times.
     */
    @GetMapping("/peak-hours")
    public AdminAnalyticsDtos.PeakHoursResponse getPeakHours(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 4): Group orders by extracted hour from createdAt timestamp
        return null;
    }

    /**
     * Task 5: Generates time-series trends based on specified granularity (daily/weekly).
     */
    @GetMapping("/trends")
    public AdminAnalyticsDtos.TrendResponse getTrends(
            @RequestParam(required = false) Long locationId,
            @RequestParam String granularity,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 5): Implement time-series grouping and date truncation queries
        return null;
    }

    /**
     * Task 6: Calculates operational performance metrics (fulfillment and delivery times)
     * utilizing the status_audit history table.
     */
    @GetMapping("/fulfillment")
    public AdminAnalyticsDtos.FulfillmentMetricsResponse getFulfillmentMetrics(
            @RequestParam(required = false) Long locationId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        // TODO (Task 6): Calculate duration differences between NEW and DELIVERED statuses from StatusAudit
        return null;
    }
}