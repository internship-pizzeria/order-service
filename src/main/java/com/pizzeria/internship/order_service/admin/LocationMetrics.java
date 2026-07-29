package com.pizzeria.internship.order_service.admin;

import java.math.BigDecimal;

public record LocationMetrics(
        Long locationId,
        String cityName,
        BigDecimal totalRevenue,
        long orderCount,
        double fulfillmentTimeMinutes
) {}