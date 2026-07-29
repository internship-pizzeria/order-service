package com.pizzeria.internship.order_service.analytics.report;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;


@Component
public class ProductPopularityReportGenerator implements  ReportGenerator {
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
        return "product_id,product_name,total_quantity,total_revenue,percentage_of_total";
    }

    @Override
    public List<String> generate(ReportRequest request) {
        String sql;
        Object[] params;
        if (request.locationId() != null) {
            sql = """
                    SELECT product_id,
                           product_name,
                           SUM(quantity) AS total_quantity,
                           SUM(total_price) AS total_revenue,
                           ROUND(SUM(total_price) * 100.0 /
                                 NULLIF(SUM(SUM(total_price)) OVER(), 0), 1) AS percentage
                    FROM report_order_items
                    WHERE created_at >= ? AND created_at < ?
                    AND location_id = ?
                    GROUP BY product_id, product_name
                    ORDER BY total_revenue DESC
                    """;
            params = new Object[]{
                    Timestamp.from(request.from()),
                    Timestamp.from(request.to()),
                    request.locationId()
            };
        } else {
            sql = """
                    SELECT product_id,
                           product_name,
                           SUM(quantity) AS total_quantity,
                           SUM(total_price) AS total_revenue,
                           ROUND(SUM(total_price) * 100.0 /
                                 NULLIF(SUM(SUM(total_price)) OVER(), 0), 1) AS percentage
                    FROM report_order_items
                    WHERE created_at >= ? AND created_at < ?
                    GROUP BY product_id, product_name
                    ORDER BY total_revenue DESC
                    """;
            params = new Object[]{
                    Timestamp.from(request.from()),
                    Timestamp.from(request.to())
            };
        }

        return analyticsJdbcTemplate.queryForList(sql, params).stream()
                .map(row -> row.get("product_id") + ","
                        + row.get("product_name") + ","
                        + row.get("total_quantity") + ","
                        + row.get("total_revenue") + ","
                        + row.get("percentage"))
                .toList();
    }
}
