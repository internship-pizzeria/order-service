# Order Service

A Spring Boot microservice for managing pizza orders in a pizzeria system. Handles order creation, validation, pricing, and retrieval of active orders by customer phone number.

## Tech Stack

- **Java 25** / **Spring Boot 4.0.7** / **Spring Cloud 2025.1.2**
- **PostgreSQL** (primary data store)
- **Redis** (dependency present, reserved for future use)
- **Spring Cloud Gateway** (scaffolding for future API gateway)
- **Lombok**, **SpringDoc OpenAPI 3** (Swagger UI)
- **Maven** build tool

## Prerequisites

- Java 25+ (Eclipse Temurin recommended)
- Docker & Docker Compose
- A running `catalog-service` on port 8081 (companion project)

## Quick Start

### 1. Start infrastructure

```bash
docker-compose up -d
```

Starts PostgreSQL (`orderDB` on port 5434), a second PostgreSQL instance (`catalogDB` on port 5433), and Redis (port 6379).

### 2. Configure environment

Copy the example env file and adjust values if needed:

```bash
cp .env.example .env
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The service starts at **http://localhost:8080**.

### 4. API documentation

Once running, open Swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
```

## API Endpoints

### Create an order

```
POST /api/v1/orders
```

```json
{
  "customerName": "Jan Kowalski",
  "phoneNumber": "+48 123 456 789",
  "deliveryAddress": "ul. Krakowska 10, Krakow",
  "locationId": 1,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

Returns `201 Created` with the persisted order, including historical product names/prices and calculated total.

### Get active orders by phone number

```
GET /api/v1/orders/phone/{phoneNumber}
```

Returns all orders with status `NEW`, `ACCEPTED`, or `IN_PROGRESS` for the given phone number. Returns `404` if none found.

## Project Structure

```
src/main/java/com/pizzeria/internship/order_service/
├── OrderServiceApplication.java          # Application entry point
├── exception/
│   └── GlobalExceptionHandler.java       # RFC 7807 ProblemDetail error handling
├── order/
│   ├── Order.java                        # JPA entity
│   ├── OrderItem.java                    # JPA entity (order line items)
│   ├── OrderController.java              # REST controllers
│   ├── OrderService.java                 # Business logic & validation
│   ├── OrderRepository.java              # Spring Data JPA repository
│   ├── OrderItemRepository.java          # Spring Data JPA repository
│   ├── Status.java                       # Order status enum
│   └── *Dto.java                         # Request/response records
└── product/
    ├── Product.java                      # Domain model (fetched from catalog)
    ├── ProductDto.java                   # DTO from catalog-service
    └── ProductClient.java                # RestClient with retry
```

## Build

```bash
# Package
./mvnw clean package

# Run tests
./mvnw test

# Docker image
docker build -t order-service .
```

## Notes

- `spring.jpa.hibernate.ddl-auto=create` — the database schema is recreated on every startup (development mode only).
- Order items store `historicalName` and `historicalPrice` snapshots so past orders remain unaffected by catalog price changes.
- `ProductClient` retries failed catalog-service calls up to 3 times with a 3-second backoff.
