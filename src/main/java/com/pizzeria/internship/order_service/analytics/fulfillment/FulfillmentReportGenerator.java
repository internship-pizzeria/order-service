package com.pizzeria.internship.order_service.analytics.fulfillment;

import com.pizzeria.internship.order_service.analytics.report.ReportGenerator;
import com.pizzeria.internship.order_service.analytics.report.ReportRequest;
import com.pizzeria.internship.order_service.analytics.report.ReportType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class FulfillmentReportGenerator implements ReportGenerator {

    private final FulfillmentService fulfillmentService;

    FulfillmentReportGenerator(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    @Override
    public ReportType getType() {
        return ReportType.FULFILLMENT;
    }

    @Override
    public String getHeader() {
        return "from_status,to_status,average_minutes,median_minutes,p95_minutes";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        FulfillmentMetricsResponse metrics = fulfillmentService.calculateMetrics(
                request.locationId(), request.from(), request.to());

        List<String> rows = new ArrayList<>();

        rows.add(escapeCsv("NEW") + "," + escapeCsv("DELIVERED") + ","
                + escapeCsv(metrics.averageTimeToDeliverMinutes()) + ","
                + escapeCsv(metrics.medianTimeToDeliverMinutes()) + ","
                + escapeCsv(metrics.p95TimeToDeliverMinutes()));

        for (var timing : metrics.averageTimePerStatus()) {
            rows.add(escapeCsv(timing.fromStatus()) + ","
                    + escapeCsv(timing.toStatus()) + ","
                    + escapeCsv(timing.averageMinutes()) + ","
                    + escapeCsv(timing.medianMinutes()) + ","
                    + escapeCsv(timing.p95Minutes()));
        }

        return rows;
    }

    private static String escapeCsv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
