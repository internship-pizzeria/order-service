package com.pizzeria.internship.order_service.admin;

import java.math.BigDecimal;

public record RevenueSummaryResponse(
        BigDecimal totalRevenue,
        long orderCount,
        BigDecimal averageOrderValue
) { }
