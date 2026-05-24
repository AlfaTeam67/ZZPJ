# TASK-08.1: Demo Data Setup Guide

## Overview

This guide explains how to set up and run the Fin-Insight application with complete demo data, including:

- ✅ **Demo User Account** - Pre-configured Keycloak user with test credentials
- ✅ **Sample Portfolios** - 3 different portfolio examples with various asset types
- ✅ **Asset Types** - Stocks (AAPL, GOOGL, MSFT, TSLA, AMZN), Crypto (BTC, ETH)
- ✅ **Transaction History** - BUY/SELL transactions with historical dates and fees
- ✅ **Price History** - 6 months of historical price snapshots for market visualization

## Quick Start

### Prerequisites

- Docker & Docker Compose installed
- Git repository cloned
- Backend folder accessible at `backend/`

### 1. Start Backend with Docker Compose

Navigate to the backend directory and start all services:

```bash
cd backend
docker-compose up -d
```

This will automatically:
- Start PostgreSQL databases (portfolio, market-data, advisor, keycloak)
- Start Keycloak with demo users
- Run all database migrations including seed scripts
- Start Eureka server and all microservices

Wait for all services to be healthy (usually 2-3 minutes):

```bash
docker-compose ps
```

Expected output should show all services as "healthy" or "running".

### 2. Verify Database Migrations

Check that all migrations completed successfully. Access portfolio database:

```bash
# Windows PowerShell
docker exec -it fin-insight-portfolio-db psql -U user -d portfolio -c "SELECT * FROM portfolios;"

# Linux/Mac
docker exec -it fin-insight-portfolio-db psql -U user -d portfolio -c "SELECT * FROM portfolios;"
```

Expected output: Should show 3 demo portfolios

```
                   id                   |                 user_id                  |          name          |         description          |         created_at         |         updated_at         
----------------------------------------+----------------------------------------+------------------------+------------------------------+-----------------------------+-----------------------------
 223e4567-e89b-12d3-a456-426614174001  | 123e4567-e89b-12d3-a456-426614174000 | Growth Portfolio       | Long-term stock investments  | 2024-05-24 12:00:00+00     | 2024-05-24 12:00:00+00
 223e4567-e89b-12d3-a456-426614174002  | 123e4567-e89b-12d3-a456-426614174000 | Crypto Holdings        | Digital assets ...           | 2024-05-24 12:00:00+00     | 2024-05-24 12:00:00+00
 223e4567-e89b-12d3-a456-426614174003  | 123e4567-e89b-12d3-a456-426614174000 | Forex Trading          | Currency exchange ...       | 2024-05-24 12:00:00+00     | 2024-05-24 12:00:00+00
 223e4567-e89b-12d3-a456-426614174004  | 123e4567-e89b-12d3-a456-426614174000 | Diversified Mixed...   | Mix of stocks, crypto...    | 2024-05-24 12:00:00+00     | 2024-05-24 12:00:00+00
```

### 3. Login with Demo User

Use these credentials in the Fin-Insight application:

**Demo User:**
- **Email/Username:** `testuser`
- **Password:** `test123`
- **Role:** `user`

**Admin User (for monitoring):**
- **Email/Username:** `admin`
- **Password:** `admin`
- **Role:** `admin`

### 4. Verify Keycloak Setup

Keycloak admin console is available at:
```
http://localhost:8080/admin
```

Admin credentials:
- **Username:** `admin`
- **Password:** `admin`

Realm: `fin-insight`

## Demo Data Details

### Portfolio 1: Growth Portfolio

Focus on long-term stock investments.

**Assets:**
| Symbol | Type  | Quantity | Avg Buy Price | Currency |
|--------|-------|----------|---------------|----------|
| AAPL   | STOCK | 50.00    | $150.50       | USD      |
| GOOGL  | STOCK | 30.00    | $125.25       | USD      |
| MSFT   | STOCK | 25.00    | $380.75       | USD      |

**Transactions:** 3 BUY transactions spanning 180 days
- AAPL: Initial + additional purchase
- GOOGL: Single investment
- MSFT: Tech sector investment

### Portfolio 2: Crypto Holdings

Digital assets and cryptocurrencies with volatility.

**Assets:**
| Symbol  | Type   | Quantity | Avg Buy Price | Currency |
|---------|--------|----------|---------------|----------|
| BTC-USD | CRYPTO | 0.50     | $58,000       | USD      |
| ETH-USD | CRYPTO | 5.00     | $2,500        | USD      |

**Transactions:** 3 BUY transactions
- BTC: 0.5 BTC purchased
- ETH: Multiple purchases (3 + 2 coins)

### Portfolio 3: Diversified Mixed Assets

Combination of stocks and cryptocurrencies.

**Assets:**
| Symbol  | Type   | Quantity | Avg Buy Price | Currency |
|---------|--------|----------|---------------|----------|
| TSLA    | STOCK  | 15.00    | $225.50       | USD      |
| AMZN    | STOCK  | 20.00    | $170.25       | USD      |
| BTC-USD | CRYPTO | 0.25     | $61,000       | USD      |

**Transactions:** 3 BUY transactions
- TSLA: Growth investment
- AMZN: Retail sector position
- BTC: Hedge position

## Price History Data

Each symbol has 3-8 historical price snapshots spanning 6 months:

### Stocks (AAPL, GOOGL, MSFT, TSLA, AMZN)
- 6-8 snapshots each
- Price range variations showing realistic movements
- Volume data for market activity
- 24h change percentages

### Cryptocurrencies (BTC-USD, ETH-USD)
- 6 snapshots each
- Higher volatility reflected in price swings
- Large trading volumes (billions)
- Recent prices: BTC ~$62,543, ETH ~$3,428

### Forex (EUR, GBP)
- 3-4 snapshots each
- PLN-based pricing
- Minimal volume (forex pairs)
- Recent rates: EUR ~4.25 PLN, GBP ~5.12 PLN

## Using Demo Data

### View Portfolios

Once logged in with `testuser`:

1. Navigate to Dashboard
2. Select "My Portfolios"
3. Choose any of the 4 demo portfolios
4. View detailed holdings and assets

### Analyze Transactions

For each portfolio:

1. Click "Details" on portfolio
2. View "Transaction History"
3. See BUY transactions with:
   - Date executed (ranging from 200 days ago to recent)
   - Quantity and price
   - Currency and fees
   - Notes/descriptions

### Chart Historical Performance

Price snapshots enable:

1. **Price Charts:** View 6-month price trends per asset
2. **Portfolio Value:** Calculate historical portfolio value
3. **Performance Analysis:** Track gains/losses over time
4. **Asset Allocation:** Visualize portfolio composition

## Database Seeding Details

### Migration Files

**Portfolio Service:**
- `V5__seed_demo_data.sql` - Contains all portfolio, asset, and transaction data

**Market Data Service:**
- `V3__seed_price_history.sql` - Contains historical price snapshots

### User ID Mapping

Demo user UUID: `123e4567-e89b-12d3-a456-426614174000`

This UUID is used throughout seed data to link all portfolios, assets, and transactions to the demo user.

### On Conflict Handling

All INSERT statements use `ON CONFLICT ... DO NOTHING` to allow:
- Safe re-running of migrations
- Idempotent seed data
- No duplicates on re-initialization

## Troubleshooting

### Migrations Not Running

Check Flyway status in service logs:

```bash
docker logs fin-insight-portfolio-manager | grep Flyway
docker logs fin-insight-market-data-service | grep Flyway
```

### No Demo Data Showing

1. Verify migrations completed (check above)
2. Confirm testuser is created:
   ```bash
   docker exec -it fin-insight-portfolio-db psql -U user -d portfolio -c "SELECT * FROM users;"
   ```
3. Check Keycloak is running:
   ```bash
   curl -s http://localhost:8080/health | jq .
   ```

### Resetting Demo Data

To clear and re-seed:

```bash
# Option 1: Full reset with volumes
docker-compose down -v
docker-compose up -d

# Option 2: Keep services, reset data
docker exec -it fin-insight-portfolio-db psql -U user -d portfolio < /path/to/reset.sql
```

## Next Steps

### Frontend Integration

1. Start frontend development server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

2. Open browser: `http://localhost:5173`

3. Login with demo credentials

### API Testing

Use Postman collections in `backend/postman/`:

1. Import `portfolio-manager-all-endpoints.postman_collection.json`
2. Set environment variable for JWT token (obtained via login)
3. Test endpoints with demo data

### Performance Testing

Generate more demo data by:
1. Duplicating V5 migration with additional portfolios
2. Creating more users in Keycloak
3. Running load tests against API endpoints

## Security Notes

⚠️ **Demo credentials and data are for development/testing only**

- Never use in production
- Clear all demo data before deployment
- Use proper user authentication in production
- Generate strong passwords for Keycloak admin

## Support

For issues with demo data setup:
1. Check service logs: `docker logs <service-name>`
2. Verify database connectivity
3. Review migration status in service startup logs
4. Check Keycloak realm configuration

---

**Created:** 2024-05-24  
**Last Updated:** 2024-05-24  
**Status:** Ready for Demo
