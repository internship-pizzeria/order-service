package com.pizzeria.internship.order_service.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
class AnalyticsDataSourceConfig {

    @Bean
    JdbcTemplate analyticsJdbcTemplate(
            @Value("${analytics.datasource.url}") String url,
            @Value("${analytics.datasource.username}") String username,
            @Value("${analytics.datasource.password}") String password) {
        HikariDataSource ds = createAnalyticsDataSource(url, username, password);
        return new JdbcTemplate(ds);
    }

    @Bean
    DataSourceInitializer analyticsDataSourceInitializer(
            @Value("${analytics.datasource.url}") String url,
            @Value("${analytics.datasource.username}") String username,
            @Value("${analytics.datasource.password}") String password) {
        HikariDataSource ds = createAnalyticsDataSource(url, username, password);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("analytics-schema.sql"));
        populator.setSeparator(";");
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(ds);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    private HikariDataSource createAnalyticsDataSource(String url, String username, String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setPoolName("analytics-hikari-pool");
        return ds;
    }
}
