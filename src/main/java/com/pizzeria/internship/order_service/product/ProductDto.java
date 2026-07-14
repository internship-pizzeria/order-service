package com.pizzeria.internship.order_service.product;

import java.math.BigDecimal;

public record ProductDto(Long id, String name, BigDecimal price) {

}
