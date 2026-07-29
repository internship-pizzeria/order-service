package com.pizzeria.internship.order_service.analytics.infrastructure;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class ReportRegistry {

    private final Map<ReportType, ReportGenerator> generators;

    ReportRegistry(List<ReportGenerator> generators) {
        this.generators = generators.stream()
                .collect(Collectors.toMap(ReportGenerator::getType, Function.identity()));
    }

    ReportGenerator getGenerator(ReportType type) {
        ReportGenerator generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("Unknown report type: " + type);
        }
        return generator;
    }
}
