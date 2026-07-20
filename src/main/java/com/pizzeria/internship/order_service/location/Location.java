package com.pizzeria.internship.order_service.location;

import lombok.Builder;

import java.util.Objects;

@Builder
class Location {
    private final Long id;
    String city;
    String postalCode;
    String street;
    String buildingNumber;
    String countryCode;
    String timezone;
    LocationStatus status;

    public static Location fromDto(LocationDto dto) {
        Objects.requireNonNull(dto, "Dto must not be null");
        return new Location(dto.id(), dto.city(), dto.postalCode(), dto.street(),
                dto.buildingNumber(), dto.countryCode(), dto.timezone(), dto.status());
    }
}
