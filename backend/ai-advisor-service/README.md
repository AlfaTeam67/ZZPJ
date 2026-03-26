# AI Advisor Service

AI-powered financial recommendation service for the Fin-Insight platform.

## Overview

This microservice provides personalized investment recommendations based on user portfolios and risk tolerance levels. It integrates with the Fin-Insight ecosystem through Spring Cloud components.

## Features

- AI-powered investment recommendations
- Risk-based portfolio analysis
- RESTful API with OpenAPI documentation
- JWT-based authentication via Keycloak
- Service discovery with Eureka
- Centralized configuration with Spring Cloud Config
- Health monitoring with Spring Boot Actuator

## Technology Stack

- Java 21
- Spring Boot 3.5.3
- Spring Cloud 2025.0.1
- Spring Security OAuth2 Resource Server
- SpringDoc OpenAPI
- Gradle
- Docker

## API Endpoints

### POST /api/recommendations
Generate personalized investment recommendations.

**Request Body:**
```json
{
  "userId": "user123",
  "portfolioId": "portfolio456",
  "riskTolerance": "MODERATE"
}
```

**Risk Tolerance Levels:**
- `LOW` - Conservative investment approach
- `MODERATE` - Balanced investment strategy
- `HIGH` - Growth-focused investments
- `AGGRESSIVE` - High-risk, high-reward strategy

**Response:**
```json
{
  "recommendations": [
    "Diversify into index funds for balanced growth",
    "Consider 60/40 stock-bond split"
  ],
  "confidence": 0.90,
  "timestamp": "2024-03-26T10:30:00"
}
```

### GET /api/recommendations/health
Simple health check endpoint.

## Configuration

The service uses Spring Cloud Config Server for centralized configuration. Configuration is loaded from `bootstrap.yml`:

```yaml
spring:
  application:
    name: ai-advisor-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
```

## Security

The service is secured using OAuth2 JWT authentication:
- All `/api/**` endpoints require authentication
- JWT tokens are validated against Keycloak
- `/actuator/health` and Swagger UI are publicly accessible

## Running the Service

### Prerequisites
- Java 21
- Docker (optional)

### Local Development

1. Start Config Server (port 8888)
2. Start Eureka Server (port 8761)
3. Run the service:

```bash
./gradlew bootRun
```

The service will start on port 8083.

### Using Docker

Build the image:
```bash
docker build -t ai-advisor-service .
```

Run the container:
```bash
docker run -p 8083:8083 ai-advisor-service
```

## Testing

Run all tests:
```bash
./gradlew test
```

## API Documentation

Once the service is running, access the Swagger UI at:
```
http://localhost:8083/swagger-ui.html
```

## Health Checks

- Application health: `http://localhost:8083/actuator/health`
- Service health: `http://localhost:8083/api/recommendations/health`

## Future Enhancements

- Integration with actual AI/ML models
- Historical recommendation tracking
- Performance analytics
- Market data integration
- Real-time recommendation updates
