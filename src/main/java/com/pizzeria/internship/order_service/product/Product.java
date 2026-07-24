package com.pizzeria.internship.order_service.product;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor(access= AccessLevel.PRIVATE)
public final class Product {

    private final Long id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final boolean available;


    public static Product fromDto(ProductDto dto) {
        Objects.requireNonNull(dto, "ProductDto must not be null");
        return new Product(dto.id(), dto.name(), dto.description(), dto.price(), dto.available());
    }

}
