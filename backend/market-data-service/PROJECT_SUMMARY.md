# Market Data Service - Project Structure

## Overview
Complete Spring Boot 3.5.3 microservice for managing market data (symbols and prices) in the Fin-Insight platform.

## ✅ Generated Components

### 1. Build Configuration
- **build.gradle** - Gradle build with all required dependencies:
  - Spring Boot 3.5.3 with Java 21
  - Spring Data JPA
  - Spring Security OAuth2 Resource Server
  - Spring Cloud Config
  - Spring Cloud Netflix Eureka
  - SpringDoc OpenAPI 2.7.0
  - PostgreSQL + Flyway
  - Lombok
  - H2 (test only)

### 2. Configuration Files
- **bootstrap.yml** - Spring Cloud Config integration
- **application.yml** - Application configuration with:
  - PostgreSQL datasource configuration
  - JPA/Hibernate settings
  - Flyway migration settings
  - OAuth2 JWT configuration (Keycloak)
  - Eureka client configuration
  - Actuator endpoints
  - SpringDoc OpenAPI settings

### 3. Entity Classes (with BigDecimal)
- **Symbol.java** - Trading symbol entity
  - Fields: id, ticker (unique), name, type, createdAt
  - Type constraint: STOCK, CRYPTO, FX
  - Index on ticker

- **MarketPrice.java** - Market price data entity
  - Fields: id, symbol (ManyToOne), price, volume, timestamp
  - **BigDecimal precision**: price (19,4), volume (19,8)
  - Indexes on symbol_id and timestamp

### 4. Repository Interfaces
- **SymbolRepository.java** - Extends JpaRepository
  - Custom method: findByTicker()

- **MarketPriceRepository.java** - Extends JpaRepository
  - Custom method: findBySymbolOrderByTimestampDesc()
  - Custom query: findLatestPrices()

### 5. DTOs
- **SymbolDto.java** - Symbol data transfer object
- **MarketPriceDto.java** - Market price data transfer object with validation

### 6. REST Controllers
- **SymbolController.java** (/api/symbols)
  - GET / - List all symbols
  - GET /{id} - Get symbol by ID
  - POST / - Create new symbol
  - Secured with JWT, OpenAPI annotations

- **MarketPriceController.java** (/api/market-prices)
  - GET /latest - Get latest prices for all symbols
  - GET /symbol/{ticker} - Get prices for specific symbol
  - POST / - Add new market price
  - Secured with JWT, OpenAPI annotations

### 7. Security Configuration
- **SecurityConfig.java**
  - JWT authentication via OAuth2 Resource Server
  - Method security enabled
  - Public endpoints: /actuator/health, /actuator/info, Swagger UI
  - Secured endpoints: /api/**
  - OpenAPI security scheme configuration (Bearer JWT)
  - Stateless session management

### 8. Flyway Migrations
- **V1__create_symbols.sql** - Creates symbols table with:
  - NUMERIC types for PostgreSQL
  - Unique constraint on ticker
  - Type constraint check
  - Index on ticker

- **V2__create_market_prices.sql** - Creates market_prices table with:
  - NUMERIC(19,4) for price
  - NUMERIC(19,8) for volume
  - Foreign key to symbols
  - Indexes on symbol_id and timestamp

### 9. Tests
- **MarketDataServiceApplicationTests.java**
  - Context loads test
  - Smoke test verifying all beans
  - Uses H2 in-memory database
  - Test-specific security config with mock JWT decoder

- **TestSecurityConfig.java**
  - Provides mock JwtDecoder for tests
  - Prevents OAuth2 configuration issues in tests

### 10. Docker
- **Dockerfile** - Multi-stage build
  - Stage 1: Build with eclipse-temurin:21-jdk-alpine
  - Stage 2: Runtime with eclipse-temurin:21-jre-alpine
  - Non-root user (spring:spring)
  - Health check on /actuator/health
  - Exposes port 8082

### 11. Documentation
- **README.md** - Comprehensive documentation including:
  - Features overview
  - Technology stack
  - Prerequisites
  - Database setup
  - Configuration guide
  - Running instructions (Gradle & Docker)
  - API endpoints
  - Testing instructions
  - Architecture notes
  - Decimal precision specifications

## ✅ Key Features Implemented

1. **High-Precision Decimals**: All financial values use BigDecimal (NO double/float)
2. **JWT Security**: OAuth2 Resource Server with Keycloak integration
3. **Service Discovery**: Eureka client registration
4. **Centralized Config**: Spring Cloud Config integration
5. **Database Migrations**: Flyway with PostgreSQL
6. **API Documentation**: Interactive Swagger UI with security
7. **Health Checks**: Actuator endpoints
8. **Docker Support**: Multi-stage production-ready Dockerfile
9. **Comprehensive Tests**: Passing smoke tests with proper test configuration

## ✅ Test Status
```
BUILD SUCCESSFUL
2 tests completed, 2 passed
```

## Service Configuration
- **Port**: 8082
- **Service Name**: market-data-service
- **Config Server**: http://localhost:8888
- **Eureka Server**: http://localhost:8761
- **Keycloak**: http://localhost:8080/realms/fininsight
- **Database**: PostgreSQL on localhost:5432/marketdata

## API Endpoints
- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI Docs: http://localhost:8082/v3/api-docs
- Health Check: http://localhost:8082/actuator/health

## Next Steps
1. Start Config Server on port 8888
2. Start Eureka Server on port 8761
3. Setup Keycloak realm 'fininsight'
4. Create PostgreSQL database 'marketdata'
5. Run the service: `./gradlew bootRun`
6. Access Swagger UI to test endpoints

## Important Notes
- ⚠️ All prices use NUMERIC(19,4) - 4 decimal places
- ⚠️ All volumes use NUMERIC(19,8) - 8 decimal places
- ⚠️ NEVER use double or float for financial data
- ⚠️ All /api/** endpoints require JWT authentication
- ✅ Tests use H2 with JPA schema generation
- ✅ Production uses PostgreSQL with Flyway migrations
