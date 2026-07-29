package com.pizzeria.internship.order_service.analytics.report;

import java.util.List;

public interface ReportGenerator {
    ReportType getType();
    String getHeader();
    List<String> generate(ReportRequest request);
}
