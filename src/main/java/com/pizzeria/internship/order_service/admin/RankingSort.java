package com.pizzeria.internship.order_service.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sorting criterion for the product ranking")
public enum RankingSort {
    BY_QUANTITY,
    BY_REVENUE
}
