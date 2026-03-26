# Portfolio Manager Microservice

Portfolio management microservice for the Fin-Insight project.

## Technologies

- **Java**: 21
- **Spring Boot**: 3.5.3
- **Build Tool**: Gradle
- **Database**: PostgreSQL with Flyway migrations
- **Security**: OAuth2 Resource Server with JWT (Keycloak)
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config Client
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, Spring Boot Test

## Features

- JWT-based authentication via Keycloak
- Role-based access control (@PreAuthorize)
- RESTful API for portfolio management
- BigDecimal precision for financial calculations (NO double/float)
- Database migrations with Flyway
- OpenAPI documentation at `/swagger-ui.html`
- Health checks at `/actuator/health`

## Prerequisites

- Java 21
- PostgreSQL database
- Config Server running on `http://localhost:8888`
- Keycloak for authentication

## Project Structure

```
src/main/java/com/fininsight/portfolio/
├── config/          # Security and application configuration
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
└── service/         # Business logic
```

## Entities

### Portfolio
- `id` (Long) - Primary key
- `name` (String) - Portfolio name
- `userId` (String) - Owner's user ID
- `totalValue` (BigDecimal) - Total portfolio value (precision 19, scale 4)
- `createdAt` (LocalDateTime) - Creation timestamp

### PortfolioPosition
- `id` (Long) - Primary key
- `portfolio` (ManyToOne) - Reference to portfolio
- `symbol` (String) - Stock/asset symbol
- `quantity` (BigDecimal) - Position quantity (precision 19, scale 8)
- `averagePrice` (BigDecimal) - Average purchase price (precision 19, scale 4)
- `currentPrice` (BigDecimal) - Current market price (precision 19, scale 4)
- `getCurrentValue()` - Calculated method: quantity × currentPrice

## API Endpoints

All endpoints require JWT authentication with `USER` role.

### Portfolios

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/portfolios` | Get all portfolios for authenticated user |
| GET | `/api/portfolios/{id}` | Get portfolio by ID |
| POST | `/api/portfolios` | Create new portfolio |
| PUT | `/api/portfolios/{id}` | Update portfolio |
| DELETE | `/api/portfolios/{id}` | Delete portfolio |

## Database Schema

### portfolios
```sql
CREATE TABLE portfolios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    total_value NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### portfolio_positions
```sql
CREATE TABLE portfolio_positions (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL REFERENCES portfolios(id),
    symbol VARCHAR(20) NOT NULL,
    quantity NUMERIC(19, 8) NOT NULL,
    average_price NUMERIC(19, 4) NOT NULL,
    current_price NUMERIC(19, 4) NOT NULL
);
```

## Running the Application

### Local Development

1. Start Config Server
2. Start PostgreSQL database
3. Start Keycloak
4. Run the application:

```bash
./gradlew bootRun
```

### Run Tests

```bash
./gradlew test
```

### Build Docker Image

```bash
docker build -t portfolio-manager:latest .
```

### Run with Docker

```bash
docker run -p 8080:8080 \
  -e SPRING_CLOUD_CONFIG_URI=http://config-server:8888 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/portfolio \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/fininsight \
  portfolio-manager:latest
```

## Configuration

The service uses Spring Cloud Config for external configuration. The `bootstrap.yml` file defines the Config Server connection.

### bootstrap.yml
```yaml
spring:
  application:
    name: portfolio-manager
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
```

## Security

- JWT tokens are validated against Keycloak
- Roles are extracted from `realm_access.roles` claim
- Method-level security with `@PreAuthorize`
- Swagger UI and health endpoints are publicly accessible

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Health Checks

```
http://localhost:8080/actuator/health
```

## Important Notes

### BigDecimal Usage
⚠️ **CRITICAL**: This service uses `BigDecimal` for all monetary and quantity values to ensure precision in financial calculations. Never use `double` or `float` for financial data.

- **Prices**: NUMERIC(19, 4)
- **Quantities**: NUMERIC(19, 8)

### Security Configuration
- The service requires a valid JWT token from Keycloak
- Users must have the `USER` role to access portfolio endpoints
- Each user can only access their own portfolios (enforced by `userId` filtering)

## License

Fin-Insight Project
