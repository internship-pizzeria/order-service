package com.pizzeria.internship.order_service.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Revenue summary for the selected time range and location scope")
public record RevenueSummaryResponse(
        @Schema(description = "Total revenue of all orders in the range", example = "15230.50")
        BigDecimal totalRevenue,

        @Schema(description = "Total number of orders in the range", example = "312")
        long orderCount,

        @Schema(description = "Average order value (total revenue / order count)", example = "48.82")
        BigDecimal averageOrderValue
) { }
