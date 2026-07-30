package com.pizzeria.internship.order_service.analytics.peakhours;

import com.pizzeria.internship.order_service.admin.AdminAnalyticsDtos;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportGenerator;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportRequest;
import com.pizzeria.internship.order_service.analytics.infrastructure.ReportType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class PeakHoursReportGenerator implements ReportGenerator {

    private final PeakHoursService peakHoursService;

    PeakHoursReportGenerator(PeakHoursService peakHoursService) {
        this.peakHoursService = peakHoursService;
    }

    @Override
    public ReportType getType() {
        return ReportType.PEAK_HOURS;
    }

    @Override
    public String getHeader() {
        return "hour;order_count;revenue;is_peak";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        AdminAnalyticsDtos.PeakHoursResponse response = peakHoursService.getPeakHours(
                request.scope(), request.from(), request.to());

        return response.hours().stream()
                .map(h -> escapeCsv(h.hour()) + ";"
                        + escapeCsv(h.orderCount()) + ";"
                        + escapeCsv(h.revenue()) + ";"
                        + escapeCsv(h.isPeak()))
                .toList();
    }

    private static String escapeCsv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
