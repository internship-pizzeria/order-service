package com.pizzeria.internship.order_service.analytics.popularity;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import com.pizzeria.internship.order_service.admin.ProductRankingResponse;
import com.pizzeria.internship.order_service.admin.RankingSort;
import com.pizzeria.internship.order_service.analytics.scope.AllLocations;
import com.pizzeria.internship.order_service.analytics.scope.AnalyticsScope;
import com.pizzeria.internship.order_service.analytics.scope.SingleLocation;
import com.pizzeria.internship.order_service.order.OrderQueryFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRankingServiceTest {

    @Mock
    private OrderQueryFacade orderQueryFacade;

    @InjectMocks
    private ProductRankingService productRankingService;

    private static final Instant FROM = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-07-28T23:59:59Z");
    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Test
    void shouldReturnEmptyResponseWhenNoProducts() {
        // GIVEN
        AnalyticsScope scope = new AllLocations();
        Page<ProductRankingItem> emptyPage = new PageImpl<>(Collections.emptyList(), PAGEABLE, 0);
        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(emptyPage);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(BigDecimal.ZERO);

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
    }

    @Test
    void shouldReturnSingleProductWith100Percent() {
        // GIVEN
        AnalyticsScope scope = new AllLocations();
        ProductRankingItem item = new ProductRankingItem(1L, "Margherita", 10L, new BigDecimal("299.90"), 0.0);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(item), PAGEABLE, 1);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(new BigDecimal("299.90"));

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertEquals(1, response.content().size());
        assertEquals(1L, response.content().getFirst().productId());
        assertEquals("Margherita", response.content().getFirst().historicalName());
        assertEquals(10L, response.content().getFirst().totalQuantity());
        assertEquals(100.0, response.content().getFirst().percentageOfTotal(), 0.1);
        assertEquals(0, response.pageNumber());
        assertEquals(20, response.pageSize());
        assertEquals(1, response.totalElements());
    }

    @Test
    void shouldCalculatePercentagesForMultipleProducts() {
        // GIVEN
        AnalyticsScope scope = new AllLocations();
        ProductRankingItem margherita = new ProductRankingItem(1L, "Margherita", 20L, new BigDecimal("599.80"), 0.0);
        ProductRankingItem pepperoni = new ProductRankingItem(2L, "Pepperoni", 10L, new BigDecimal("349.90"), 0.0);
        ProductRankingItem hawaiian = new ProductRankingItem(3L, "Hawaiian", 5L, new BigDecimal("149.95"), 0.0);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(margherita, pepperoni, hawaiian), PAGEABLE, 3);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(new BigDecimal("1099.65"));

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertEquals(3, response.content().size());
        assertEquals(54.5, response.content().get(0).percentageOfTotal(), 0.1);
        assertEquals(31.8, response.content().get(1).percentageOfTotal(), 0.1);
        assertEquals(13.6, response.content().get(2).percentageOfTotal(), 0.1);
    }

    @Test
    void shouldSortByQuantityWhenSpecified() {
        // GIVEN
        AnalyticsScope scope = new SingleLocation(1L);
        ProductRankingItem pepperoni = new ProductRankingItem(2L, "Pepperoni", 50L, new BigDecimal("1499.50"), 0.0);
        ProductRankingItem margherita = new ProductRankingItem(1L, "Margherita", 10L, new BigDecimal("299.90"), 0.0);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(pepperoni, margherita), PAGEABLE, 2);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(new BigDecimal("1799.40"));

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_QUANTITY, PAGEABLE);

        // THEN
        assertEquals(2, response.content().size());
        assertEquals(50L, response.content().get(0).totalQuantity());
        assertEquals(10L, response.content().get(1).totalQuantity());
    }

    @Test
    void shouldSortByRevenueWhenSpecified() {
        // GIVEN
        AnalyticsScope scope = new AllLocations();
        ProductRankingItem margherita = new ProductRankingItem(1L, "Margherita", 10L, new BigDecimal("599.80"), 0.0);
        ProductRankingItem pepperoni = new ProductRankingItem(2L, "Pepperoni", 20L, new BigDecimal("349.90"), 0.0);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(margherita, pepperoni), PAGEABLE, 2);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(new BigDecimal("949.70"));

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertEquals(2, response.content().size());
        assertEquals(new BigDecimal("599.80"), response.content().get(0).totalRevenue());
        assertEquals(new BigDecimal("349.90"), response.content().get(1).totalRevenue());
    }

    @Test
    void shouldReturnZeroPercentageWhenTotalRevenueIsZero() {
        // GIVEN
        AnalyticsScope scope = new AllLocations();
        ProductRankingItem item = new ProductRankingItem(1L, "Margherita", 5L, new BigDecimal("149.95"), 0.0);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(item), PAGEABLE, 1);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(BigDecimal.ZERO);

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertEquals(0.0, response.content().getFirst().percentageOfTotal(), 0.1);
    }

    @Test
    void shouldFilterByLocationId() {
        // GIVEN
        AnalyticsScope scope = new SingleLocation(5L);
        Page<ProductRankingItem> page = new PageImpl<>(List.of(
                new ProductRankingItem(1L, "Margherita", 15L, new BigDecimal("449.85"), 0.0)
        ), PAGEABLE, 1);

        when(orderQueryFacade.getProductRanking(eq(scope), eq(FROM), eq(TO), any()))
                .thenReturn(page);
        when(orderQueryFacade.getTotalRevenue(eq(scope), eq(FROM), eq(TO)))
                .thenReturn(new BigDecimal("449.85"));

        // WHEN
        ProductRankingResponse response =
                productRankingService.getProductRanking(scope, FROM, TO, RankingSort.BY_REVENUE, PAGEABLE);

        // THEN
        assertEquals(1, response.content().size());
        assertEquals(100.0, response.content().getFirst().percentageOfTotal(), 0.1);
    }
}
