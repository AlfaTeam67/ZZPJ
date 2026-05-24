# Quick Start - Demo Data

## One-Command Setup

```bash
docker-compose up -d
```

Wait 2-3 minutes for all services to be healthy.

## Demo Login Credentials

**Username:** `testuser`  
**Password:** `test123`

## What You Get

- 3 demo portfolios (Growth, Crypto, Mixed)
- 10 assets across stocks and crypto
- 9 transactions with realistic historical dates
- 6 months of price history for charting

## Verify Setup

```bash
# Check services are running
docker-compose ps

# Verify databases initialized with seed data
docker exec -it fin-insight-portfolio-db psql -U user -d portfolio -c "SELECT COUNT(*) as portfolios FROM portfolios;"
```

Expected output: `portfolios | 3`

## Access Points

- **Frontend:** http://localhost:5173 (after `npm run dev`)
- **Keycloak Admin:** http://localhost:8080/admin
- **Eureka Dashboard:** http://localhost:8761
- **Portfolio API:** http://localhost:8081

## Full Documentation

See [DEMO.md](./DEMO.md) for complete setup guide and demo data details.
