package com.pizzeria.internship.order_service.analytics.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
class RevenueReportGenerator implements ReportGenerator {

    private final JdbcTemplate analyticsJdbcTemplate;

    RevenueReportGenerator(JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    @Override
    public ReportType getType() {
        return ReportType.REVENUE;
    }

    @Override
    public String getHeader() {
        return "location_id;total_revenue;order_count";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        String sql;
        Object[] params;
        if (request.locationId() != null) {
            sql = """
                    SELECT location_id,
                           SUM(total_price) AS total_revenue,
                           COUNT(DISTINCT order_id) AS order_count
                    FROM report_order_items
                    WHERE created_at >= ? AND created_at < ?
                    AND location_id = ?
                    GROUP BY location_id
                    ORDER BY total_revenue DESC
                    """;
            params = new Object[]{
                    Timestamp.from(request.from()),
                    Timestamp.from(request.to()),
                    request.locationId()
            };
        } else {
            sql = """
                    SELECT location_id,
                           SUM(total_price) AS total_revenue,
                           COUNT(DISTINCT order_id) AS order_count
                    FROM report_order_items
                    WHERE created_at >= ? AND created_at < ?
                    GROUP BY location_id
                    ORDER BY total_revenue DESC
                    """;
            params = new Object[]{
                    Timestamp.from(request.from()),
                    Timestamp.from(request.to())
            };
        }

        return analyticsJdbcTemplate.queryForList(sql, params).stream()
                .map(row -> escapeCsv(row.get("location_id")) + ";"
                        + escapeCsv(row.get("total_revenue")) + ";"
                        + escapeCsv(row.get("order_count")))
                .toList();
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