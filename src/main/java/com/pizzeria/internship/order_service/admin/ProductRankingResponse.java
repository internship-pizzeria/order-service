package com.pizzeria.internship.order_service.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated product ranking")
public record ProductRankingResponse(
        @Schema(description = "Ranked products of the current page")
        List<ProductRankingItem> content,

        @Schema(description = "Current page number, zero-based", example = "0")
        int pageNumber,

        @Schema(description = "Number of items per page", example = "20")
        int pageSize,

        @Schema(description = "Total number of ranked products across all pages", example = "42")
        long totalElements
) {}
