package com.pizzeria.internship.order_service.analytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
interface DailyLocationStatsRepository extends JpaRepository<DailyLocationStats, Long> {

    @Query("""
            SELECT COALESCE(SUM(d.totalRevenue), 0) as totalRevenue, COALESCE(SUM(d.orderCount), 0) as orderCount
            FROM DailyLocationStats d
            WHERE (:locationId IS NULL OR d.locationId = :locationId)
            AND d.statsDate >= :fromDate
            AND d.statsDate < :toDate
            """)
    RevenueAggregation getAggregatedStats(
            @Param("locationId") Long locationId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Modifying
    @Query(value = """
            INSERT INTO daily_location_stats (stats_date, location_id, total_revenue, order_count)
            VALUES (:statsDate, :locationId, :revenue, :orderCount)
            ON CONFLICT (stats_date, location_id)
            DO UPDATE SET total_revenue = EXCLUDED.total_revenue, order_count = EXCLUDED.order_count
            """, nativeQuery = true)
    void upsertStats(
            @Param("statsDate") LocalDate statsDate,
            @Param("locationId") Long locationId,
            @Param("revenue") BigDecimal revenue,
            @Param("orderCount") long orderCount
    );

    interface RevenueAggregation {
        BigDecimal getTotalRevenue();
        long getOrderCount();
    }
}
