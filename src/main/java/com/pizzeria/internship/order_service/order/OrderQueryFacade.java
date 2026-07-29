package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private final JdbcTemplate analyticsJdbcTemplate;

    public List<DailyAggregation> getDailyAggregations(Instant from, Instant to) {
        return orderRepository.getDailyAggregations(from, to);
    }

    public Page<ProductRankingItem> getProductRanking(
            Long locationId, Instant from, Instant to,
            Pageable pageable) {

        Timestamp fromTs = Timestamp.from(from);
        Timestamp toTs = Timestamp.from(to);
        String whereClause;
        Object[] countParams;
        Object[] dataParams;

        if (locationId != null) {
            whereClause = "AND location_id = ?";
            countParams = new Object[]{fromTs, toTs, locationId};
            dataParams = new Object[]{fromTs, toTs, locationId,
                    pageable.getPageSize(), pageable.getOffset()};
        } else {
            whereClause = "";
            countParams = new Object[]{fromTs, toTs};
            dataParams = new Object[]{fromTs, toTs,
                    pageable.getPageSize(), pageable.getOffset()};
        }

        long total = analyticsJdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT product_id) FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + whereClause,
                Long.class, countParams);

        List<ProductRankingItem> items = analyticsJdbcTemplate.query(
                "SELECT product_id, product_name, " +
                "SUM(quantity) AS total_quantity, " +
                "SUM(total_price) AS total_revenue " +
                "FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + whereClause + " " +
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

    public BigDecimal getTotalRevenue(Long locationId, Instant from, Instant to) {
        String whereClause;
        Object[] params;
        if (locationId != null) {
            whereClause = "AND location_id = ?";
            params = new Object[]{Timestamp.from(from), Timestamp.from(to), locationId};
        } else {
            whereClause = "";
            params = new Object[]{Timestamp.from(from), Timestamp.from(to)};
        }

        BigDecimal result = analyticsJdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(total_price), 0) FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " + whereClause,
                BigDecimal.class, params);
        return result != null ? result : BigDecimal.ZERO;
    }
}
