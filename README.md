# Product Service

Enterprise-grade Product Master Management Microservice built with Java 21 and Spring Boot 3.x.

## Overview

This microservice is responsible **only** for **Product Master Management**.  
It does **NOT** manage inventory, stock, orders, purchases, billing, pricing, or warehouses.

## Technology Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.x |
| Spring Security | 6.x (JWT) |
| Spring Data JPA | 3.x |
| PostgreSQL | 15+ |
| Lombok | Latest |
| SpringDoc OpenAPI | 2.5.0 |
| JUnit 5 / Mockito | Latest |
| Maven | 3.9+ |

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+

### Database Setup

```sql
CREATE DATABASE product_db;
```

### Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/product_db
spring.datasource.username=your_username
spring.datasource.******
jwt.secret=your-256-bit-secret-key
```

### Build & Run

```bash
# Build the project
mvn clean package -DskipTests

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/product-service-0.0.1-SNAPSHOT.jar
```

### Running Tests

```bash
mvn test
```

## API Documentation

Once the application is running, access Swagger UI at:

```
http://localhost:8081/swagger-ui/index.html
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/products` | Create a new product |
| `GET` | `/api/products` | Get all products (paginated) |
| `GET` | `/api/products/{id}` | Get product by ID |
| `GET` | `/api/products/search` | Search products |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

### Pagination & Sorting

```
GET /api/products?page=0&size=20&sort=name
```

### Search Filters

```
GET /api/products/search?keyword=laptop&category=Electronics&brand=Dell&status=ACTIVE
```

## Authentication

All product endpoints require a valid JWT token in the `Authorization` header:

```
Authorization: ******
```

Public endpoints (no auth required):

- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`
- `GET /actuator/**`

## Package Structure

```
src/main/java/com/example/product_service/
├── config/          # OpenAPI configuration
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter & security configuration
├── service/         # Business logic
└── ProductServiceApplication.java

src/test/java/com/example/product_service/
├── controller/      # Controller layer tests
├── security/        # Security utility tests
└── service/         # Service layer tests
```

## Future Integrations

- **Kafka** — Event publishing for ProductCreated, ProductUpdated, ProductDeleted
- **Redis** — Product caching
- **MapStruct** — DTO mapping
- **Flyway** — Database migrations
- **Docker / Kubernetes** — Containerisation and orchestration
