package com.pizzeria.internship.order_service.admin;

import java.util.List;

public record ProductRankingResponse(
        List<ProductRankingItem> content,
        int pageNumber,
        int pageSize,
        long totalElements
) {}