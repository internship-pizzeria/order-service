package com.pizzeria.internship.order_service.analytics;

public record AllLocations() implements AnalyticsScope {

    @Override
    public String sqlSuffix() {
        return "";
    }

    @Override
    public Object[] sqlParams() {
        return new Object[0];
    }

    @Override
    public Long extractLocationId() {
        return null;
    }

    @Override
    public String describe() {
        return "national";
    }
}
