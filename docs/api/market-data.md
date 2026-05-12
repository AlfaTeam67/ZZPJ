# 📈 Market Data Service API

**Base URL:** `http://localhost:8082/api`  
**Auth:** JWT Bearer Token (wymagany)

---

## Symbole (Symbols)

### GET /api/symbols

Pobierz listę wszystkich dostępnych symboli.

**Request:**
```bash
curl -X GET http://localhost:8082/api/symbols \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
[
  {
    "symbol": "AAPL",
    "type": "STOCK",
    "apiSource": "ALPHA_VANTAGE",
    "baseCurrency": "USD",
    "active": true,
    "addedAt": "2024-01-10T08:00:00Z"
  },
  {
    "symbol": "BTC",
    "type": "CRYPTO",
    "apiSource": "COINGECKO",
    "baseCurrency": "USD",
    "active": true,
    "addedAt": "2024-01-12T09:30:00Z"
  },
  {
    "symbol": "EURUSD",
    "type": "FOREX",
    "apiSource": "FIXER",
    "baseCurrency": "EUR",
    "active": true,
    "addedAt": "2024-01-14T10:15:00Z"
  }
]
```

---

### GET /api/symbols/{symbol}

Pobierz szczegóły konkretnego symbolu.

**Request:**
```bash
curl -X GET http://localhost:8082/api/symbols/AAPL \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
{
  "symbol": "AAPL",
  "type": "STOCK",
  "apiSource": "ALPHA_VANTAGE",
  "baseCurrency": "USD",
  "active": true,
  "addedAt": "2024-01-10T08:00:00Z"
}
```

**Errors:**
- `404` – Symbol nie znaleziony

---

### POST /api/symbols

Dodaj nowy symbol.

**Request:**
```bash
curl -X POST http://localhost:8082/api/symbols \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "GOOGL",
    "type": "STOCK",
    "apiSource": "ALPHA_VANTAGE",
    "baseCurrency": "USD",
    "active": true
  }'
```

**Body:**
```json
{
  "symbol": "string (required, max 10)",
  "type": "STOCK|CRYPTO|FOREX|ETF|COMMODITY (required)",
  "apiSource": "string (required)",
  "baseCurrency": "string (required, ISO 4217)",
  "active": "boolean (required)"
}
```

**Response (201):**
```json
{
  "symbol": "GOOGL",
  "type": "STOCK",
  "apiSource": "ALPHA_VANTAGE",
  "baseCurrency": "USD",
  "active": true,
  "addedAt": "2024-01-20T15:30:00Z"
}
```

**Errors:**
- `400` – Niepoprawne dane
- `409` – Symbol już istnieje

---

## Ceny rynkowe (Market Prices)

### GET /api/market-prices/latest

Pobierz najnowsze ceny dla wszystkich aktywnych symboli.

**Request:**
```bash
curl -X GET http://localhost:8082/api/market-prices/latest \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440005",
    "symbol": "AAPL",
    "source": "ALPHA_VANTAGE",
    "price": 155.50,
    "currency": "USD",
    "changePct24h": 2.15,
    "volume24h": 52000000,
    "fetchedAt": "2024-01-20T14:30:00Z"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440006",
    "symbol": "BTC",
    "source": "COINGECKO",
    "price": 42500.00,
    "currency": "USD",
    "changePct24h": 3.45,
    "volume24h": 28000000000,
    "fetchedAt": "2024-01-20T14:28:00Z"
  }
]
```

---

### GET /api/market-prices/symbol/{ticker}

Pobierz historię cen dla konkretnego symbolu.

**Request:**
```bash
curl -X GET http://localhost:8082/api/market-prices/symbol/AAPL \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440005",
    "symbol": "AAPL",
    "source": "ALPHA_VANTAGE",
    "price": 155.50,
    "currency": "USD",
    "changePct24h": 2.15,
    "volume24h": 52000000,
    "fetchedAt": "2024-01-20T14:30:00Z"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440007",
    "symbol": "AAPL",
    "source": "ALPHA_VANTAGE",
    "price": 152.30,
    "currency": "USD",
    "changePct24h": 1.85,
    "volume24h": 48000000,
    "fetchedAt": "2024-01-20T13:30:00Z"
  }
]
```

**Errors:**
- `404` – Symbol nie znaleziony

---

### POST /api/market-prices

Dodaj nową cenę rynkową.

**Request:**
```bash
curl -X POST http://localhost:8082/api/market-prices \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "source": "ALPHA_VANTAGE",
    "price": 156.75,
    "currency": "USD",
    "changePct24h": 2.45,
    "volume24h": 55000000,
    "fetchedAt": "2024-01-20T15:00:00Z"
  }'
```

**Body:**
```json
{
  "symbol": "string (required)",
  "source": "string (required)",
  "price": "number (required, > 0)",
  "currency": "string (required, ISO 4217)",
  "changePct24h": "number (optional)",
  "volume24h": "number (optional, >= 0)",
  "fetchedAt": "datetime (optional, ISO 8601)"
}
```

**Response (201):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440008",
  "symbol": "AAPL",
  "source": "ALPHA_VANTAGE",
  "price": 156.75,
  "currency": "USD",
  "changePct24h": 2.45,
  "volume24h": 55000000,
  "fetchedAt": "2024-01-20T15:00:00Z"
}
```

**Errors:**
- `400` – Niepoprawne dane
- `404` – Symbol nie znaleziony

---

## Typy symboli

| Typ | Opis | Przykład |
|-----|------|---------|
| `STOCK` | Akcje | AAPL, GOOGL, MSFT |
| `CRYPTO` | Kryptowaluty | BTC, ETH, XRP |
| `FOREX` | Pary walutowe | EURUSD, GBPUSD |
| `ETF` | Fundusze ETF | SPY, QQQ, VTI |
| `COMMODITY` | Surowce | GOLD, OIL, NATURAL_GAS |

---

## Źródła danych (API Sources)

| Źródło | Opis | Typ |
|--------|------|------|
| `ALPHA_VANTAGE` | Akcje, forex, crypto | REST API |
| `COINGECKO` | Kryptowaluty | REST API |
| `FIXER` | Kursy walut | REST API |
| `YAHOO_FINANCE` | Akcje, ETF | Web scraping |
| `MANUAL` | Dane wpisane ręcznie | Manual |

---

## Caching

Ceny są cachowane w Redis na **5 minut**. Po wygaśnięciu cache'u dane są pobierane z API.

```bash
# Sprawdzenie cache'u
redis-cli
> GET market:price:AAPL
```

---

## Rate Limiting

- **Limit:** 100 requestów na minutę
- **Header:** `X-RateLimit-Remaining`

```bash
curl -X GET http://localhost:8082/api/market-prices/latest \
  -H "Authorization: Bearer <TOKEN>" \
  -i
```

Odpowiedź:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1705776660
```

---

## Błędy

| Kod | Opis |
|-----|------|
| `200` | OK |
| `201` | Created |
| `400` | Bad Request |
| `401` | Unauthorized |
| `404` | Not Found |
| `429` | Too Many Requests (rate limit) |
| `500` | Internal Server Error |
