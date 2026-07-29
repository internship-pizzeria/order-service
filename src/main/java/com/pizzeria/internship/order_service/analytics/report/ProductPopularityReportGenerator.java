package com.pizzeria.internship.order_service.analytics.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;


@Component
class ProductPopularityReportGenerator implements ReportGenerator {
    private final JdbcTemplate analyticsJdbcTemplate;

    ProductPopularityReportGenerator(JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    @Override
    public ReportType getType() {
        return ReportType.POPULARITY;
    }

    @Override
    public String getHeader() {
        return "product_id;product_name;total_quantity;total_revenue;percentage_of_total";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        Object[] baseParams = new Object[]{
                Timestamp.from(request.from()),
                Timestamp.from(request.to())
        };
        Object[] params = concat(baseParams, request.scope().sqlParams());

        String sql = "SELECT product_id, product_name, " +
                     "SUM(quantity) AS total_quantity, " +
                     "SUM(total_price) AS total_revenue, " +
                     "ROUND(SUM(total_price) * 100.0 / " +
                     "NULLIF(SUM(SUM(total_price)) OVER(), 0), 1) AS percentage " +
                     "FROM report_order_items " +
                     "WHERE created_at >= ? AND created_at < ? " +
                     request.scope().sqlSuffix() + " " +
                     "GROUP BY product_id, product_name " +
                     "ORDER BY total_revenue DESC";

        return analyticsJdbcTemplate.queryForList(sql, params).stream()
                .map(row -> escapeCsv(row.get("product_id")) + ";"
                        + escapeCsv(row.get("product_name")) + ";"
                        + escapeCsv(row.get("total_quantity")) + ";"
                        + escapeCsv(row.get("total_revenue")) + ";"
                        + escapeCsv(row.get("percentage")))
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
