package com.pizzeria.internship.order_service.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated location performance results")
public record LocationPerformancePageResponse(
        @Schema(description = "Location metrics of the current page")
        List<LocationMetrics> content,

        @Schema(description = "Current page number, zero-based", example = "0")
        int pageNumber,

        @Schema(description = "Number of items per page", example = "20")
        int pageSize,

        @Schema(description = "Total number of locations across all pages", example = "5")
        long totalElements
) {}
