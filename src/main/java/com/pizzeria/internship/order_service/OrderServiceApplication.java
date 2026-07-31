package com.pizzeria.internship.order_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@OpenAPIDefinition(
        info = @Info(
                title = "Order Service API",
                version = "1.0",
                description = "REST API of the Pizzera order management system. " +
                        "Handles the full order lifecycle (creation, status tracking and validated status transitions), " +
                        "exposes admin-only analytics (revenue summaries, product rankings, location performance, " +
                        "peak-hours and fulfillment metrics) and provides CSV report generation. " +
                        "Authenticated endpoints expect the 'X-User-Id' and 'LocationId' HTTP headers, which are " +
                        "populated by the API gateway."
        )
)
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
						.withJdbcTemplate(new org.springframework.jdbc.core.JdbcTemplate(dataSource))
						.usingDbTime()
						.build()
		);
	}

}
