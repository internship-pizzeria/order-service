package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.analytics.AnalyticsScope;
import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderRepository orderRepository;
    private final @Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate;

    public List<DailyAggregation> getDailyAggregations(Instant from, Instant to) {
        return orderRepository.getDailyAggregations(from, to);
    }

    public Page<ProductRankingItem> getProductRanking(
            AnalyticsScope scope, Instant from, Instant to,
            Pageable pageable) {

        Timestamp fromTs = Timestamp.from(from);
        Timestamp toTs = Timestamp.from(to);
        Object[] scopeParams = scope.sqlParams();

        Object[] countParams = concat(
                new Object[]{fromTs, toTs},
                scopeParams
        );

        Object[] dataParams = concat(
                new Object[]{fromTs, toTs},
                scopeParams,
                new Object[]{pageable.getPageSize(), pageable.getOffset()}
        );

        long total = analyticsJdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT product_id) FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + scope.sqlSuffix(),
                Long.class, countParams);

        List<ProductRankingItem> items = analyticsJdbcTemplate.query(
                "SELECT product_id, product_name, " +
                "SUM(quantity) AS total_quantity, " +
                "SUM(total_price) AS total_revenue " +
                "FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + scope.sqlSuffix() + " " +
                "GROUP BY product_id, product_name " +
                "ORDER BY total_revenue DESC " +
                "LIMIT ? OFFSET ?",
                (rs, rowNum) -> new ProductRankingItem(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getLong("total_quantity"),
                        rs.getBigDecimal("total_revenue"),
                        0.0
                ),
                dataParams);

        return new PageImpl<>(items, pageable, total);
    }

    public BigDecimal getTotalRevenue(AnalyticsScope scope, Instant from, Instant to) {
        Object[] params = concat(
                new Object[]{Timestamp.from(from), Timestamp.from(to)},
                scope.sqlParams()
        );

        BigDecimal result = analyticsJdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_price), 0) FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + scope.sqlSuffix(),
                BigDecimal.class, params);
        return result != null ? result : BigDecimal.ZERO;
    }

    private static Object[] concat(Object[]... arrays) {
        int total = 0;
        for (Object[] arr : arrays) total += arr.length;
        Object[] result = new Object[total];
        int dest = 0;
        for (Object[] arr : arrays) {
            System.arraycopy(arr, 0, result, dest, arr.length);
            dest += arr.length;
        }
        return result;
    }
}
