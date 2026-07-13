package com.pizzeria.internship.order_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		Dotenv.configure().systemProperties().load();
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}
