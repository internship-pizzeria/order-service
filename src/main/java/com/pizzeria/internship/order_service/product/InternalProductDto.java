package com.pizzeria.internship.order_service.product;

import java.math.BigDecimal;

record InternalProductDto(Long id, String name, BigDecimal price, Boolean available) {
    Product toProduct() {
        return Product.builder()
                .id(id).name(name).price(price)
                .description("")
                .available(available != null && available)
                .build();
    }
}
