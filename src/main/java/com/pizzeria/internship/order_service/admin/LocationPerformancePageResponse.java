package com.pizzeria.internship.order_service.admin;

import java.util.List;

public record LocationPerformancePageResponse(
        List<LocationMetrics> content,
        int pageNumber,
        int pageSize,
        long totalElements
) {}