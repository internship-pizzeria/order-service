package com.pizzeria.internship.order_service.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
class AnalyticsDataSourceConfig {

    @Bean
    JdbcTemplate analyticsJdbcTemplate(
            @Value("${analytics.datasource.url}") String url,
            @Value("${analytics.datasource.username}") String username,
            @Value("${analytics.datasource.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return new JdbcTemplate(ds);
    }
}