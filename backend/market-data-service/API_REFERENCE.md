# Market Data Service - API Quick Reference

## Base URL
```
http://localhost:8082
```

## Authentication
All `/api/**` endpoints require JWT Bearer token:
```
Authorization: Bearer <your-jwt-token>
```

## Symbol Endpoints

### 1. List All Symbols
```http
GET /api/symbols
```

**Response:**
```json
[
  {
    "id": 1,
    "ticker": "AAPL",
    "name": "Apple Inc.",
    "type": "STOCK",
    "createdAt": "2024-03-26T19:00:00"
  }
]
```

### 2. Get Symbol by ID
```http
GET /api/symbols/{id}
```

**Response:**
```json
{
  "id": 1,
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "type": "STOCK",
  "createdAt": "2024-03-26T19:00:00"
}
```

### 3. Create Symbol
```http
POST /api/symbols
Content-Type: application/json
```

**Request Body:**
```json
{
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "type": "STOCK"
}
```

**Valid Types:** `STOCK`, `CRYPTO`, `FX`

**Response:** 201 Created
```json
{
  "id": 1,
  "ticker": "AAPL",
  "name": "Apple Inc.",
  "type": "STOCK",
  "createdAt": "2024-03-26T19:00:00"
}
```

## Market Price Endpoints

### 1. Get Latest Prices
```http
GET /api/market-prices/latest
```

**Response:**
```json
[
  {
    "id": 1,
    "ticker": "AAPL",
    "price": "150.2500",
    "volume": "45678901.23456789",
    "timestamp": "2024-03-26T19:00:00"
  }
]
```

### 2. Get Prices by Symbol
```http
GET /api/market-prices/symbol/{ticker}
```

**Example:**
```http
GET /api/market-prices/symbol/AAPL
```

**Response:**
```json
[
  {
    "id": 2,
    "ticker": "AAPL",
    "price": "150.5000",
    "volume": "45678901.23456789",
    "timestamp": "2024-03-26T19:05:00"
  },
  {
    "id": 1,
    "ticker": "AAPL",
    "price": "150.2500",
    "volume": "45678901.23456789",
    "timestamp": "2024-03-26T19:00:00"
  }
]
```

### 3. Add Market Price
```http
POST /api/market-prices
Content-Type: application/json
```

**Request Body:**
```json
{
  "ticker": "AAPL",
  "price": "150.2500",
  "volume": "45678901.23456789",
  "timestamp": "2024-03-26T19:00:00"
}
```

**Response:** 201 Created
```json
{
  "id": 1,
  "ticker": "AAPL",
  "price": "150.2500",
  "volume": "45678901.23456789",
  "timestamp": "2024-03-26T19:00:00"
}
```

## Actuator Endpoints (Public)

### Health Check
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP"
}
```

### Application Info
```http
GET /actuator/info
```

## API Documentation (Public)

### Swagger UI
```
http://localhost:8082/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8082/v3/api-docs
```

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-03-26T19:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/symbols"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-03-26T19:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/symbols"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-03-26T19:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Symbol not found",
  "path": "/api/symbols/999"
}
```

## Data Validation

### Symbol
- `ticker`: Required, max 20 characters, must be unique
- `name`: Required, max 255 characters
- `type`: Required, must be one of: STOCK, CRYPTO, FX

### Market Price
- `ticker`: Required, must exist in symbols table
- `price`: Required, must be greater than 0, max 19 digits with 4 decimals
- `volume`: Required, must be >= 0, max 19 digits with 8 decimals
- `timestamp`: Required, ISO-8601 format

## Decimal Precision
- **Prices**: NUMERIC(19,4) - Example: `150.2500`
- **Volumes**: NUMERIC(19,8) - Example: `45678901.23456789`
- **CRITICAL**: Always use BigDecimal, never double/float

## Example: Complete Workflow

### 1. Create a symbol
```bash
curl -X POST http://localhost:8082/api/symbols \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "name": "Apple Inc.",
    "type": "STOCK"
  }'
```

### 2. Add market price
```bash
curl -X POST http://localhost:8082/api/market-prices \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "ticker": "AAPL",
    "price": "150.2500",
    "volume": "45678901.23456789",
    "timestamp": "2024-03-26T19:00:00"
  }'
```

### 3. Get latest prices
```bash
curl http://localhost:8082/api/market-prices/latest \
  -H "Authorization: Bearer <token>"
```

### 4. Get prices for specific symbol
```bash
curl http://localhost:8082/api/market-prices/symbol/AAPL \
  -H "Authorization: Bearer <token>"
```

## Notes
- All timestamps use ISO-8601 format (e.g., `2024-03-26T19:00:00`)
- All endpoints return JSON
- Authentication uses JWT tokens from Keycloak
- Service registers with Eureka as `market-data-service`
- Configuration loaded from Config Server at startup
