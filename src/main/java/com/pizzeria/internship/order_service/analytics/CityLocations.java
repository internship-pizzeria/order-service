package com.pizzeria.internship.order_service.analytics;

import java.util.List;
import java.util.stream.Collectors;

public record CityLocations(String cityName, List<Long> locationIds) implements AnalyticsScope {

    @Override
    public String sqlSuffix() {
        if (locationIds.isEmpty()) return "AND 1=0";
        return "AND location_id IN (" + locationIds.stream().map(x -> "?").collect(Collectors.joining(",")) + ")";
    }

    @Override
    public Object[] sqlParams() {
        return locationIds.toArray();
    }

    @Override
    public Long extractLocationId() {
        return null;
    }

    @Override
    public String describe() {
        return "city_" + cityName;
    }
}
