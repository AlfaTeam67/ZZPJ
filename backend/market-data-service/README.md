# Market Data Service

Market Data Service microservice for the Fin-Insight project. This service manages financial market data including symbols and real-time price information.

## Features

- **Symbol Management**: Create and retrieve trading symbols (stocks, cryptocurrencies, forex)
- **Market Price Data**: Store and retrieve market prices with high-precision decimal values
- **JWT Authentication**: Secured with OAuth2 JWT tokens from Keycloak
- **Service Discovery**: Integrated with Eureka for service registration
- **Centralized Configuration**: Uses Spring Cloud Config Server
- **API Documentation**: Interactive Swagger UI at `/swagger-ui.html`

## Technology Stack

- Java 21
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Spring Cloud Config
- Spring Cloud Netflix Eureka
- PostgreSQL
- Flyway (Database Migrations)
- SpringDoc OpenAPI
- Lombok
- Gradle

## Prerequisites

- Java 21
- PostgreSQL 15+
- Keycloak (for authentication)
- Config Server (running on port 8888)
- Eureka Server (running on port 8761)

## Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE marketdata;
```

## Configuration

The service uses Spring Cloud Config Server. Create configuration in your config repository:

```yaml
# market-data-service.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/marketdata
    username: postgres
    password: your-password
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/fininsight
```

## Running the Application

### Using Gradle

```bash
./gradlew bootRun
```

### Using Docker

Build the image:
```bash
docker build -t market-data-service:latest .
```

Run the container:
```bash
docker run -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/marketdata \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  market-data-service:latest
```

## API Endpoints

### Symbols
- `GET /api/symbols` - Get all symbols
- `GET /api/symbols/{id}` - Get symbol by ID
- `POST /api/symbols` - Create new symbol

### Market Prices
- `GET /api/market-prices/latest` - Get latest prices for all symbols
- `GET /api/market-prices/symbol/{ticker}` - Get prices for specific symbol
- `POST /api/market-prices` - Add new market price

### Actuator
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info

### API Documentation
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs

## Running Tests

```bash
./gradlew test
```

## Architecture Notes

### Decimal Precision
- Prices use `NUMERIC(19,4)` (19 digits total, 4 decimal places)
- Volumes use `NUMERIC(19,8)` (19 digits total, 8 decimal places)
- **CRITICAL**: All financial values use `BigDecimal`, never `double` or `float`

### Security
- All `/api/**` endpoints require JWT authentication
- Actuator health endpoint is public
- Swagger UI is accessible without authentication for development

### Database Migrations
Flyway migrations are in `src/main/resources/db/migration/`:
- `V1__create_symbols.sql` - Creates symbols table
- `V2__create_market_prices.sql` - Creates market_prices table with foreign key

## Port

Default port: **8082**

## Service Registration

The service registers with Eureka as `market-data-service`.
