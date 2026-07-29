package com.pizzeria.internship.order_service.analytics.infrastructure;

import java.util.List;

public interface ReportGenerator {
    ReportType getType();
    String getHeader();
    List<String> generate(ReportRequest request);
}
