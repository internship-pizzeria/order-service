package com.pizzeria.internship.order_service.admin;

import com.pizzeria.internship.order_service.analytics.fulfillment.FulfillmentMetricsResponse;
import com.pizzeria.internship.order_service.analytics.fulfillment.FulfillmentService;
import com.pizzeria.internship.order_service.analytics.peakhours.PeakHoursService;
import com.pizzeria.internship.order_service.analytics.performance.LocationPerformanceService;
import com.pizzeria.internship.order_service.analytics.popularity.ProductRankingService;
import com.pizzeria.internship.order_service.analytics.revenue.OrderAnalyticsFacade;
import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Admin Analytics", description = "Analytics endpoints used by the admin panel. All endpoints require the " +
        "'X-User-Id' and 'LocationId' HTTP headers and are scoped to a single location when 'locationId' is provided, " +
        "otherwise they aggregate data across all locations.")
class AdminAnalyticsController {

    private final OrderAnalyticsFacade orderAnalyticsFacade;
    private final ProductRankingService productRankingService;
    private final FulfillmentService fulfillmentService;
    private final LocationPerformanceService locationPerformanceService;
    private final PeakHoursService peakHoursService;

    @GetMapping("/revenue")
    @Operation(
            summary = "Get revenue summary",
            description = "Returns total revenue, order count and average order value for the selected time range " +
                    "and location scope.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Revenue summary for the given range"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public RevenueSummaryResponse getRevenueSummary(
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
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
    @Operation(
            summary = "Get product ranking",
            description = "Returns the most popular products (by quantity or revenue) within the selected time range, " +
                    "including each product's share of the total revenue. Results are paginated (page, size, sort).",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1")),
                    @Parameter(name = "page", description = "Zero-based page index", example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Number of items per page", example = "20",
                            schema = @Schema(type = "integer", defaultValue = "20")),
                    @Parameter(name = "sort", description = "Sorting criteria, e.g. 'totalRevenue:desc'. Repeatable for multiple sort keys.",
                            example = "totalRevenue:desc",
                            schema = @Schema(type = "string", defaultValue = "totalRevenue:desc"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated product ranking"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProductRankingResponse getProductRanking(
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
            @RequestParam Instant to,
            @Parameter(description = "Sorting criterion of the ranking", schema = @Schema(implementation = RankingSort.class))
            @RequestParam(defaultValue = "BY_REVENUE") RankingSort sortBy,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        AnalyticsScope scope = resolveScope(locationId);
        return productRankingService.getProductRanking(scope, from, to, sortBy, pageable);
    }

    @GetMapping("/locations")
    @Operation(
            summary = "Get location performance",
            description = "Returns performance metrics (revenue, order count, average fulfillment time) for every " +
                    "location within the selected time range. Results are paginated (page, size, sort).",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1")),
                    @Parameter(name = "page", description = "Zero-based page index", example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Number of items per page", example = "20",
                            schema = @Schema(type = "integer", defaultValue = "20")),
                    @Parameter(name = "sort", description = "Sorting criteria, e.g. 'totalRevenue:desc'. Repeatable for multiple sort keys.",
                            example = "totalRevenue:desc",
                            schema = @Schema(type = "string", defaultValue = "totalRevenue:desc"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated location performance data"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public LocationPerformancePageResponse getLocationPerformance(
            @Parameter(description = "Metric used to sort the locations. Valid values: REVENUE, ORDER_COUNT, FULFILLMENT_TIME.", example = "REVENUE")
            @RequestParam(defaultValue = "REVENUE") String metric,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
            @RequestParam Instant to,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return locationPerformanceService.getLocationPerformance(metric, from, to, pageable);
    }

    @GetMapping("/peak-hours")
    @Operation(
            summary = "Get peak hours",
            description = "Returns order count and revenue aggregated per hour of the day (0-23) for the selected time " +
                    "range. Hours with an order count exceeding the configured threshold are flagged as peak hours.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hour-by-hour order statistics"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AdminAnalyticsDtos.PeakHoursResponse getPeakHours(
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
            @RequestParam Instant to) {
        AnalyticsScope scope = resolveScope(locationId);
        return peakHoursService.getPeakHours(scope, from, to);
    }

    @GetMapping("/trends")
    @Operation(
            summary = "Get order trends",
            description = "Time-series aggregation of order count and revenue grouped by the requested granularity. " +
                    "NOTE: this endpoint is not implemented yet and currently returns an empty response.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time-series trend data"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AdminAnalyticsDtos.TrendResponse getTrends(
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Grouping granularity of the time series. Valid values: HOURLY, DAILY, WEEKLY, MONTHLY.", example = "DAILY", required = true)
            @RequestParam String granularity,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
            @RequestParam Instant to) {
        // TODO (Task 5): Implement time-series grouping and date truncation queries
        return null;
    }

    @GetMapping("/fulfillment")
    @Operation(
            summary = "Get fulfillment metrics",
            description = "Returns average, median and p95 time to deliver an order, plus the average time spent in " +
                    "each status transition, for the selected time range and location scope.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "X-User-Id", required = true,
                            description = "Identifier of the authenticated user, set by the API gateway.",
                            schema = @Schema(type = "integer", format = "int64", example = "42")),
                    @Parameter(in = ParameterIn.HEADER, name = "LocationId", required = true,
                            description = "Identifier of the location the request is scoped to.",
                            schema = @Schema(type = "integer", format = "int64", example = "1"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fulfillment metrics for the given range"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid 'from'/'to' parameters",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id or LocationId header",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public FulfillmentMetricsResponse getFulfillmentMetrics(
            @Parameter(description = "Optional location to narrow the scope to. When omitted, all locations are included.", example = "1")
            @RequestParam(required = false) Long locationId,
            @Parameter(description = "Start of the time range (inclusive). ISO-8601 instant.", example = "2026-07-01T00:00:00Z", required = true)
            @RequestParam Instant from,
            @Parameter(description = "End of the time range (exclusive). ISO-8601 instant.", example = "2026-08-01T00:00:00Z", required = true)
            @RequestParam Instant to) {
        return fulfillmentService.calculateMetrics(locationId, from, to);
    }

    static AnalyticsScope resolveScope(Long locationId) {
        return locationId != null ? new SingleLocation(locationId) : new AllLocations();
    }
}
