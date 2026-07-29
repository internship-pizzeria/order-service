package com.pizzeria.internship.order_service.analytics;

public sealed interface AnalyticsScope permits AllLocations, SingleLocation, CityLocations {

    String sqlSuffix();

    Object[] sqlParams();

    Long extractLocationId();

    String describe();
}
