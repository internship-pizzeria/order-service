package com.pizzeria.internship.order_service.analytics.report;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
class LocationPerformanceReportGenerator implements ReportGenerator {

    private final JdbcTemplate analyticsJdbcTemplate;

    LocationPerformanceReportGenerator(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    @Override
    public ReportType getType() {
        return ReportType.LOCATION_PERFORMANCE;
    }

    @Override
    public String getHeader() {
        return "location_id;total_revenue;order_count";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        Object[] baseParams = new Object[]{
                Timestamp.from(request.from()),
                Timestamp.from(request.to())
        };
        Object[] params = concat(baseParams, request.scope().sqlParams());

        String sql = "SELECT location_id, " +
                "SUM(total_price) AS total_revenue, " +
                "COUNT(DISTINCT order_id) AS order_count " +
                "FROM report_order_items " +
                "WHERE created_at >= ? AND created_at < ? " +
                request.scope().sqlSuffix() + " " +
                "GROUP BY location_id " +
                "ORDER BY total_revenue DESC";

        return analyticsJdbcTemplate.queryForList(sql, params).stream()
                .map(row -> escapeCsv(row.get("location_id")) + ";"
                        + escapeCsv(row.get("total_revenue")) + ";"
                        + escapeCsv(row.get("order_count")))
                .toList();
    }

    private static Object[] concat(Object[] a, Object[] b) {
        Object[] result = new Object[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
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