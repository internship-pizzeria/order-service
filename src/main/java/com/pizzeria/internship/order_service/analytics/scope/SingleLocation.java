package com.pizzeria.internship.order_service.analytics.scope;

public record SingleLocation(Long locationId) implements AnalyticsScope {

    @Override
    public String sqlSuffix() {
        return "AND location_id = ?";
    }

    @Override
    public Object[] sqlParams() {
        return new Object[]{locationId};
    }

    @Override
    public Long extractLocationId() {
        return locationId;
    }

    @Override
    public String describe() {
        return "location_" + locationId;
    }
}
