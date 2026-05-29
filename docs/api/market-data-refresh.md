# Market Data Refresh & SSE Stream

## SSE Price Stream

### `GET /api/prices/stream`

Streams real-time price updates to subscribers via Server-Sent Events.

- **Auth**: `permitAll` — market prices are public data
- **Content-Type**: `text/event-stream`
- **Timeout**: none (0 = infinite, heartbeat ping every ~25 s keeps connection alive)

**Initial event**: Sent immediately on connection with the current snapshot of all active symbol prices.

**Subsequent events**: Emitted after each scheduler run (`refreshStocks`, `refreshCrypto`, `eodRefresh`).

**Event data format** (JSON array of `MarketPriceResponse`):
```json
[
  {
    "id": "uuid",
    "symbol": "AAPL",
    "source": "finnhub",
    "price": 182.50,
    "currency": "USD",
    "changePct24h": 1.25,
    "fetchedAt": "2026-05-28T10:00:00Z"
  }
]
```

**Heartbeat**: Named `ping` event sent every 25 s — browser `EventSource` ignores it (no `onmessage` trigger).

---

## Admin Refresh Endpoints

Both endpoints require `ROLE_ADMIN`.

### `POST /api/prices/refresh/{symbol}`

Triggers an immediate Finnhub fetch for the given symbol, persists the snapshot, and broadcasts via SSE.

| Parameter | Type | Description |
|-----------|------|-------------|
| `symbol`  | path | Ticker, e.g. `AAPL`, `BTC` |

**Responses**:
- `204 No Content` — fetch and persist succeeded
- `404 Not Found` — symbol not active or unknown
- `403 Forbidden` — caller lacks `ADMIN` role

### `POST /api/prices/refresh/all`

Triggers refresh for all active STOCK, FOREX, and CRYPTO symbols sequentially.

**Responses**: `204 No Content`

---

## Scheduler Crons

| Method | Trigger | Types |
|--------|---------|-------|
| `refreshStocks` | `0 */5 9-23 * * MON-FRI` (UTC) | STOCK, FOREX |
| `refreshCrypto` | `0 */5 * * * *` (configurable) | CRYPTO |
| `eodRefresh`    | `0 0 23 * * MON-FRI` (UTC)     | STOCK |

Override via config server: `market-data.scheduler.cron`, `market-data.scheduler.cron-crypto`.
