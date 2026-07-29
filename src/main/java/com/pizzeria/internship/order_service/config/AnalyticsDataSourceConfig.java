package com.pizzeria.internship.order_service.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AnalyticsDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsDataSourceConfig.class);

    @Primary
    @Bean(destroyMethod = "close")
    DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setPoolName("primary-hikari-pool");
        return ds;
    }

    @Bean(destroyMethod = "close")
    HikariDataSource analyticsDataSource(
            @Value("${analytics.datasource.url}") String url,
            @Value("${analytics.datasource.username}") String username,
            @Value("${analytics.datasource.password}") String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setPoolName("analytics-hikari-pool");
        return ds;
    }

    @Primary
    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate analyticsJdbcTemplate(@Qualifier("analyticsDataSource") DataSource analyticsDataSource) {
        return new JdbcTemplate(analyticsDataSource);
    }
}
