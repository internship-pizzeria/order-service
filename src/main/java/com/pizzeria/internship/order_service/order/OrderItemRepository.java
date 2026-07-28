package com.pizzeria.internship.order_service.order;

import com.pizzeria.internship.order_service.admin.ProductRankingItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
            SELECT new com.pizzeria.internship.order_service.admin.ProductRankingItem(
                oi.productId,
                oi.historicalName,
                SUM(oi.quantity),
                SUM(oi.historicalPrice * oi.quantity),
                0.0
            )
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.createdAt >= :from AND o.createdAt < :to
            AND o.status != 'REJECTED'
            AND (:locationId IS NULL OR o.locationId = :locationId)
            GROUP BY oi.productId, oi.historicalName
            """)
    Page<ProductRankingItem> getProductRanking(
            @Param("locationId") Long locationId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(oi.historicalPrice * oi.quantity), 0)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.createdAt >= :from AND o.createdAt < :to
            AND o.status != 'REJECTED'
            AND (:locationId IS NULL OR o.locationId = :locationId)
            """)
    BigDecimal getTotalRevenue(
            @Param("locationId") Long locationId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
