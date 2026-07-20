package com.pizzeria.internship.order_service.location;

public record LocationDto(Long id, String city, String postalCode, String street, String buildingNumber,
                          String countryCode, String timezone, LocationStatus status) {
}
