package com.pizzeria.internship.order_service.order;

import java.math.BigDecimal;

public record DailyAggregation(
        Long locationId,
        BigDecimal totalRevenue,
        long orderCount
) {}
