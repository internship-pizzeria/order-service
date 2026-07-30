package com.pizzeria.internship.order_service.analytics.sync;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
class AnalyticsSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsSchemaInitializer.class);
    private final JdbcTemplate analyticsJdbcTemplate;

    AnalyticsSchemaInitializer(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    @PostConstruct
    void init() {
        try {
            String sql = new String(new ClassPathResource("analytics-schema.sql").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            analyticsJdbcTemplate.execute(sql);

            analyticsJdbcTemplate.execute("""
                    ALTER TABLE report_jobs
                    ADD COLUMN IF NOT EXISTS file_content TEXT
                    """);

            log.info("Analytics DB schema initialized successfully");
        } catch (IOException e) {
            log.error("Failed to read analytics-schema.sql: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to initialize analytics DB schema: {}", e.getMessage());
        }
    }
}
