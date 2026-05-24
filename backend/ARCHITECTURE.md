# 🏗️ Fin-Insight Architecture Guide

## System Overview

```
                        ┌─────────────────┐
                        │  React Frontend │
                        │ Port 5173       │
                        └────────┬────────┘
                                 │
                    ┌────────────▼────────────┐
                    │    KEYCLOAK (OAuth2)    │
                    │    Port 8080            │
                    │ (Authentication)        │
                    └────────┬────────────────┘
                             │
                    ┌────────▼──────────┐
                    │ API GATEWAY       │
                    │ Port 8084         │
                    │ (Request routing) │
                    └─────┬──┬──┬───────┘
                          │  │  │
        ┌─────────────────┼──┼──┼──────────────┐
        │                 │  │  │              │
   ┌────▼────┐    ┌────┬──▼┐ │ ┌────┬──────┐
   │Portfolio │    │ Market   │ │ AI        │
   │Manager   │    │ Data     │ │ Advisor   │
   │8081      │    │ 8082     │ │ 8083      │
   └────┬────┘    └────┬─────┘ └────┬──────┘
        │              │            │
   ┌────▼────┐    ┌────▼─────┐ ┌───▼───────┐
   │Portfolio │    │Market    │ │ Advisor   │
   │Database  │    │Data DB   │ │ Database  │
   │Port 5433 │    │Port 5434 │ │ Port 5435 │
   └──────────┘    └──────────┘ └───────────┘

┌──────────────────────────────────────────────┐
│     INFRASTRUCTURE SERVICES                  │
├──────────────────────────────────────────────┤
│  Eureka Server (8761) - Service Discovery    │
│  Config Server (8888) - Centralized Config   │
│  Redis (6379) - Cache/Session Store          │
│  Keycloak DB (5432) - Auth Database          │
└──────────────────────────────────────────────┘
```

---

## 🔧 Core Services

### 1. API Gateway
**Port**: 8084  
**Role**: Request routing, load balancing, authentication  
**Tech**: Spring Cloud Gateway  

```yaml
Routes:
  /api/portfolios/** → Portfolio Manager (8081)
  /api/market-prices/** → Market Data Service (8082)
  /api/recommendations/** → AI Advisor Service (8083)
```

**Features**:
- Route predicate matching
- Load balancing (Round Robin)
- Circuit breaker for resilience
- JWT token validation

---

### 2. Portfolio Manager Service
**Port**: 8081  
**Database**: PostgreSQL (portfolio DB, port 5433)  
**Migrations**: Flyway (5 migrations)

#### Database Schema
```sql
-- Users
CREATE TABLE users (
  id UUID PRIMARY KEY,
  created_at TIMESTAMP
);

-- Portfolios
CREATE TABLE portfolios (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  name VARCHAR(255),
  description TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Assets
CREATE TABLE assets (
  id UUID PRIMARY KEY,
  portfolio_id UUID NOT NULL,
  type ENUM('STOCK', 'CRYPTO', 'BOND'),
  symbol VARCHAR(20),
  quantity DECIMAL(18,8),
  avg_buy_price DECIMAL(18,4),
  currency VARCHAR(3),
  added_at TIMESTAMP,
  FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

-- Transactions (BUY/SELL/TRANSFER)
CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  asset_id UUID,
  portfolio_id UUID NOT NULL,
  type ENUM('BUY', 'SELL', 'TRANSFER'),
  quantity DECIMAL(18,8),
  price DECIMAL(18,4),
  fee DECIMAL(18,2),
  executed_at TIMESTAMP,
  notes TEXT
);
```

#### Demo Data (V5 Migration)
```
3 Portfolios:
├─ Growth Portfolio (3 stocks)
│  ├─ AAPL: 50 units
│  ├─ GOOGL: 30 units
│  └─ MSFT: 25 units
│
├─ Crypto Holdings (2 crypto)
│  ├─ BTC-USD: 0.5 units
│  └─ ETH-USD: 10 units
│
└─ Diversified Mixed (3 assets)
   ├─ TSLA: 20 units
   ├─ AMZN: 15 units
   └─ BTC-USD: 0.25 units

Total: 10 assets, 9 transactions
```

---

### 3. Market Data Service
**Port**: 8082  
**Database**: PostgreSQL (marketdata DB, port 5434)  
**Migrations**: Flyway (3 migrations)

#### Database Schema
```sql
-- Supported Symbols
CREATE TABLE supported_symbols (
  symbol VARCHAR(20) PRIMARY KEY,
  type ENUM('STOCK', 'CRYPTO', 'BOND'),
  api_source VARCHAR(50),
  active BOOLEAN,
  base_currency VARCHAR(3),
  added_at TIMESTAMP
);

-- Price Snapshots (Real-time + Historical)
CREATE TABLE price_snapshots (
  id UUID PRIMARY KEY,
  symbol VARCHAR(20) NOT NULL,
  source VARCHAR(50),
  price DECIMAL(18,4),
  currency VARCHAR(3),
  change_pct_24h DECIMAL(6,2),
  volume_24h DECIMAL(20,0),
  fetched_at TIMESTAMP,
  FOREIGN KEY (symbol) REFERENCES supported_symbols(symbol)
);
```

#### Seed Data (V2 + V3 Migrations)
```
7 Supported Symbols:
├─ STOCKS (5): AAPL, GOOGL, MSFT, TSLA, AMZN
└─ CRYPTO (2): BTC-USD, ETH-USD

Price Snapshots per Symbol:
├─ AAPL: 8 snapshots (180 → 0 days)
├─ GOOGL: 6 snapshots (150 → 0 days)
├─ MSFT: 5 snapshots (120 → 0 days)
├─ TSLA: 4 snapshots (100 → 0 days)
├─ AMZN: 4 snapshots (90 → 0 days)
├─ BTC-USD: 8 snapshots (180 → 0 days)
└─ ETH-USD: 8 snapshots (180 → 0 days)

Total: 48 price snapshots (6 months history)
```

**API Endpoints**:
```
GET /api/market-prices/latest
  → Returns current prices for all symbols

GET /api/market-prices/{symbol}/history
  → Returns historical prices (6 months)

GET /api/supported-symbols
  → Returns list of available symbols
```

---

### 4. AI Advisor Service
**Port**: 8083  
**Database**: PostgreSQL (advisor DB, port 5435)  
**Migrations**: Flyway (6 migrations)

#### Database Schema
```sql
-- LLM Providers (OpenAI, Claude, etc.)
CREATE TABLE llm_providers (
  id UUID PRIMARY KEY,
  name VARCHAR(100),
  api_key_encrypted TEXT,
  model_name VARCHAR(100),
  is_active BOOLEAN
);

-- Recommendations (AI-generated)
CREATE TABLE recommendations (
  id UUID PRIMARY KEY,
  portfolio_id UUID NOT NULL,
  symbol VARCHAR(20),
  action VARCHAR(20), -- 'BUY', 'SELL', 'HOLD', 'REDUCE'
  confidence DECIMAL(3,1), -- 0.0 - 100.0
  reason TEXT,
  llm_provider_id UUID,
  created_at TIMESTAMP
);

-- News Cache (for recommendation context)
CREATE TABLE news_cache (
  id UUID PRIMARY KEY,
  symbol VARCHAR(20),
  title VARCHAR(255),
  content TEXT,
  source VARCHAR(100),
  fetched_at TIMESTAMP
);
```

**Recommendation Engine**:
1. Fetches portfolio data from Portfolio Manager
2. Fetches market data (prices, history, trends)
3. Fetches relevant news from News Cache
4. Sends to LLM (Claude/GPT) for analysis
5. Returns structured recommendations

**Example Recommendation**:
```json
{
  "symbol": "BTC-USD",
  "action": "INCREASE",
  "confidence": 78.5,
  "reason": "Strong uptrend detected over 3 months, low correlation with portfolio, favorable risk-reward ratio"
}
```

---

## 🔑 Key Technologies

### Authentication & Authorization
**Provider**: Keycloak 24.0  
**Protocol**: OAuth2 / OpenID Connect  
**Realm**: fin-insight  
**Demo User**: testuser / test123  

```
Keycloak Flow:
1. User logs in at Frontend
2. Frontend redirects to Keycloak login page
3. Keycloak validates credentials
4. Keycloak issues JWT token
5. Frontend includes JWT in API requests
6. Gateway validates JWT and routes to services
7. Services extract user info from token claims
```

### Database Management
**Tool**: Flyway 9.22.3  
**Strategy**: Version control for schema  

```
Migration Pattern:
V1__create_portfolios.sql
V2__create_portfolio_positions.sql
V3__rebuild_uuid_portfolio_model.sql
V4__transactions_asset_id_set_null.sql
V5__seed_demo_data.sql

Execution: Sequential, with checksums
Rollback: Manual (maintain data integrity)
```

### Service Discovery
**Server**: Eureka 2.0  
**Port**: 8761  
**Feature**: Dynamic service registration  

```
Eureka Workflow:
1. Each service registers itself at startup
2. Eureka stores: Service name, host, port, status
3. Gateway queries Eureka for routing
4. If service goes down, Eureka marks as DOWN
5. Gateway removes from load balancing
6. Service heartbeat every 30 seconds
```

### Configuration Management
**Server**: Spring Cloud Config Server  
**Port**: 8888  
**Repository**: Local filesystem (backend/config)  

```
Config Properties:
application.yml (shared config)
application-docker.yml (Docker-specific)
application-migration.yml (DB migration config)
```

---

## 🐳 Docker Architecture

### Compose Services

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| keycloak | quay.io/keycloak/keycloak:24.0 | 8080 | OAuth2 Provider |
| keycloak-db | postgres:16-alpine | internal | Keycloak DB |
| portfolio-manager | fin-insight-backend-portfolio-manager | 8081 | Portfolio Service |
| portfolio-db | postgres:16-alpine | 5433 | Portfolio DB |
| market-data-service | fin-insight-backend-market-data-service | 8082 | Market Data Service |
| market-data-db | postgres:16-alpine | 5434 | Market Data DB |
| ai-advisor-service | fin-insight-backend-ai-advisor-service | 8083 | AI Advisor Service |
| advisor-db | postgres:16-alpine | 5435 | Advisor DB |
| gateway-service | fin-insight-backend-gateway-service | 8084 | API Gateway |
| config-server | fin-insight-backend-config-server | 8888 | Config Server |
| eureka-server | fin-insight-backend-eureka-server | 8761 | Service Registry |
| redis | redis:7-alpine | 6379 | Cache (optional) |

### Network
**Name**: fin-insight-network (custom bridge)  
**Type**: Docker internal network  

Allows all containers to communicate by service name:
```
portfolio-manager:8081
market-data-service:8082
ai-advisor-service:8083
```

### Volumes
```yaml
keycloak-db-data      → /var/lib/postgresql/data (Keycloak DB)
portfolio-db-data     → /var/lib/postgresql/data (Portfolio DB)
market-data-db-data   → /var/lib/postgresql/data (Market Data DB)
advisor-db-data       → /var/lib/postgresql/data (Advisor DB)
```

---

## 📊 Data Flow

### Portfolio Creation Flow
```
1. Frontend → POST /api/portfolios
   ↓
2. Gateway → Route to Portfolio Manager (8081)
   ↓
3. Portfolio Manager → Validate request
   ↓
4. Portfolio Manager → INSERT INTO portfolios
   ↓
5. Database → Return portfolio ID
   ↓
6. Portfolio Manager → Return 201 Created
   ↓
7. Gateway → Return response to Frontend
```

### Market Data Update Flow
```
1. Market Data Service (startup)
   ↓
2. Check supported_symbols table
   ↓
3. For each symbol:
   - Fetch from API (alphavantage for stocks, coingecko for crypto)
   - INSERT/UPDATE price_snapshots
   - Keep 6-month history
   ↓
4. Schedule periodic updates (every 15 minutes)
   ↓
5. Frontend queries: GET /api/market-prices/latest
```

### AI Recommendation Flow
```
1. Frontend → GET /api/recommendations/{portfolioId}
   ↓
2. AI Advisor Service:
   - Fetch portfolio from Portfolio Manager
   - Fetch market data (current + history)
   - Fetch relevant news
   ↓
3. AI Advisor → Call LLM (Claude/GPT)
   - Provide context: portfolio, market data, news
   - Ask for recommendations
   ↓
4. LLM Response → Parse and structure
   ↓
5. Return recommendations to Frontend
   ↓
6. Cache results in recommendations table
```

---

## 🔐 Security Architecture

### Authentication Layers
```
┌─────────────────────────────────────┐
│ Frontend (React)                    │
│ - Stores JWT in localStorage        │
│ - Sends JWT in Authorization header │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ API Gateway (Spring Cloud Gateway)  │
│ - Validates JWT signature           │
│ - Checks token expiration           │
│ - Extracts user claims              │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ Individual Services                 │
│ - Validate JWT again (defense in    │
│ - Extract user info from claims     │
│ - Apply role-based access control   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│ Database                            │
│ - No direct authentication          │
│ - Service-to-DB connection pooling  │
└─────────────────────────────────────┘
```

### CORS Configuration
```yaml
Gateway CORS:
  allowedOrigins: "http://localhost:5173"
  allowedMethods: "GET, POST, PUT, DELETE"
  allowedHeaders: "Authorization, Content-Type"
  allowCredentials: true
```

---

## 📈 Scaling Strategy

### Horizontal Scaling
```
# Option 1: Scale Portfolio Manager
docker run -e SERVICE_NAME=portfolio-manager \
  -e EUREKA_URL=http://eureka-server:8761 \
  fin-insight-portfolio-manager

# Eureka auto-registers new instance
# Gateway auto-discovers and load-balances

# Option 2: Scale Market Data Service
docker run -e SERVICE_NAME=market-data-service \
  -e EUREKA_URL=http://eureka-server:8761 \
  fin-insight-market-data-service
```

### Database Scaling
```
Current: Database per Service (separate PostgreSQL instances)
Limitation: No cross-database transactions

Future Improvements:
1. Read replicas for read-heavy services
2. Connection pooling (HikariCP - already enabled)
3. Caching layer (Redis - available)
4. Event-driven architecture (async communication)
```

### Load Balancing
**Current**: Spring Cloud Gateway (Round Robin)  
**Future**: Add Nginx/HAProxy for reverse proxy

---

## 🚨 Health Checks

### Liveness Probes
```
Endpoint: GET /actuator/health/liveness

Response:
{
  "status": "UP",
  "components": {
    "diskSpace": { "status": "UP" },
    "db": { "status": "UP" }
  }
}
```

### Readiness Probes
```
Endpoint: GET /actuator/health/readiness

Response:
{
  "status": "UP",
  "components": {
    "livenessProbe": { "status": "UP" },
    "readinessProbe": { "status": "UP" }
  }
}
```

---

## 📚 Additional Resources

- **Architecture Documentation**: `/docs/architecture.md`
- **API Documentation**: `/docs/api/`
- **Database Conventions**: `/docs/conventions.md`
- **Keycloak Guide**: `/backend/postman/KEYCLOAK_AUTH_GUIDE.md`
- **Demo Data**: `/backend/DEMO.md`
- **Quick Start**: `/backend/QUICK_START_DEMO.md`

---

**Last Updated**: 2026-05-24  
**Version**: 1.0  
**Status**: Production-Ready ✅
