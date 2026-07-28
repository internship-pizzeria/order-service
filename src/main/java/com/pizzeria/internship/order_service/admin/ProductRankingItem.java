package com.pizzeria.internship.order_service.admin;

import java.math.BigDecimal;

public record ProductRankingItem(
        Long productId,
        String historicalName,
        long totalQuantity,
        BigDecimal totalRevenue,
        double percentageOfTotal
) {}