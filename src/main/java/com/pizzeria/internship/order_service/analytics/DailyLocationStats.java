package com.pizzeria.internship.order_service.analytics;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_location_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stats_date", "location_id"})
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyLocationStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stats_date", nullable = false)
    private LocalDate statsDate;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "order_count", nullable = false)
    private long orderCount;
}
