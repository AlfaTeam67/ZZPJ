# 📊 Portfolio Manager API

**Base URL:** `http://localhost:8081/api/portfolios`  
**Auth:** JWT Bearer Token (wymagany)

---

## Portfele

### GET /api/portfolios

Pobierz wszystkie portfele zalogowanego użytkownika.

**Request:**
```bash
curl -X GET http://localhost:8081/api/portfolios \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Mój portfel",
    "currency": "PLN",
    "description": "Portfel akcji i ETF",
    "createdAt": "2024-01-15T10:30:00Z",
    "totalValue": 50000.00,
    "assets": []
  }
]
```

---

### GET /api/portfolios/{id}

Pobierz konkretny portfel po ID.

**Request:**
```bash
curl -X GET http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Mój portfel",
  "currency": "PLN",
  "description": "Portfel akcji i ETF",
  "createdAt": "2024-01-15T10:30:00Z",
  "totalValue": 50000.00,
  "assets": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "symbol": "AAPL",
      "quantity": 10,
      "currentPrice": 150.50,
      "totalValue": 1505.00
    }
  ]
}
```

**Errors:**
- `404` – Portfel nie znaleziony
- `403` – Brak dostępu do portfela

---

### POST /api/portfolios

Utwórz nowy portfel.

**Request:**
```bash
curl -X POST http://localhost:8081/api/portfolios \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Nowy portfel",
    "currency": "PLN",
    "description": "Portfel dla inwestycji długoterminowych"
  }'
```

**Body:**
```json
{
  "name": "string (required, max 100)",
  "currency": "string (required, format: ISO 4217)",
  "description": "string (optional, max 500)"
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Nowy portfel",
  "currency": "PLN",
  "description": "Portfel dla inwestycji długoterminowych",
  "createdAt": "2024-01-20T14:45:00Z",
  "totalValue": 0.00,
  "assets": []
}
```

**Errors:**
- `400` – Niepoprawne dane (np. brakuje nazwy)
- `409` – Portfel o tej nazwie już istnieje

---

### PUT /api/portfolios/{id}

Zaktualizuj portfel.

**Request:**
```bash
curl -X PUT http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Zaktualizowana nazwa",
    "description": "Nowy opis"
  }'
```

**Body:**
```json
{
  "name": "string (optional)",
  "description": "string (optional)"
}
```

**Response (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Zaktualizowana nazwa",
  "currency": "PLN",
  "description": "Nowy opis",
  "createdAt": "2024-01-15T10:30:00Z",
  "totalValue": 50000.00,
  "assets": []
}
```

**Errors:**
- `404` – Portfel nie znaleziony
- `403` – Brak dostępu do portfela

---

### DELETE /api/portfolios/{id}

Usuń portfel.

**Request:**
```bash
curl -X DELETE http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (204):** No Content

**Errors:**
- `404` – Portfel nie znaleziony
- `403` – Brak dostępu do portfela
- `409` – Nie można usunąć portfela z aktywami

---

## Aktywa (Assets)

### POST /api/portfolios/{portfolioId}/assets

Dodaj aktywo do portfela.

**Request:**
```bash
curl -X POST http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000/assets \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "quantity": 10,
    "purchasePrice": 150.50,
    "purchaseDate": "2024-01-15"
  }'
```

**Body:**
```json
{
  "symbol": "string (required, max 10)",
  "quantity": "number (required, > 0)",
  "purchasePrice": "number (required, > 0)",
  "purchaseDate": "date (required, format: YYYY-MM-DD)"
}
```

**Response (201):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "symbol": "AAPL",
  "quantity": 10,
  "purchasePrice": 150.50,
  "currentPrice": 155.00,
  "totalValue": 1550.00,
  "gainLoss": 45.00,
  "gainLossPercent": 2.99
}
```

**Errors:**
- `400` – Niepoprawne dane
- `404` – Portfel nie znaleziony
- `409` – Aktywo już istnieje w portfelu

---

### DELETE /api/portfolios/{portfolioId}/assets/{assetId}

Usuń aktywo z portfela.

**Request:**
```bash
curl -X DELETE http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000/assets/660e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (204):** No Content

**Errors:**
- `404` – Portfel lub aktywo nie znalezione
- `403` – Brak dostępu

---

## Transakcje (Transactions)

### GET /api/portfolios/{portfolioId}/transactions

Pobierz historię transakcji portfela.

**Request:**
```bash
curl -X GET http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000/transactions \
  -H "Authorization: Bearer <TOKEN>"
```

**Response (200):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "assetId": "660e8400-e29b-41d4-a716-446655440001",
    "symbol": "AAPL",
    "type": "BUY",
    "quantity": 10,
    "price": 150.50,
    "totalAmount": 1505.00,
    "transactionDate": "2024-01-15T10:30:00Z",
    "commission": 0.00
  },
  {
    "id": "770e8400-e29b-41d4-a716-446655440003",
    "assetId": "660e8400-e29b-41d4-a716-446655440001",
    "symbol": "AAPL",
    "type": "SELL",
    "quantity": 5,
    "price": 155.00,
    "totalAmount": 775.00,
    "transactionDate": "2024-01-20T14:45:00Z",
    "commission": 5.00
  }
]
```

---

### POST /api/portfolios/{portfolioId}/transactions

Utwórz nową transakcję.

**Request:**
```bash
curl -X POST http://localhost:8081/api/portfolios/550e8400-e29b-41d4-a716-446655440000/transactions \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "assetId": "660e8400-e29b-41d4-a716-446655440001",
    "type": "BUY",
    "quantity": 5,
    "price": 155.00,
    "transactionDate": "2024-01-20",
    "commission": 5.00
  }'
```

**Body:**
```json
{
  "assetId": "uuid (required)",
  "type": "BUY|SELL (required)",
  "quantity": "number (required, > 0)",
  "price": "number (required, > 0)",
  "transactionDate": "date (required, format: YYYY-MM-DD)",
  "commission": "number (optional, >= 0)"
}
```

**Response (201):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440004",
  "assetId": "660e8400-e29b-41d4-a716-446655440001",
  "symbol": "AAPL",
  "type": "BUY",
  "quantity": 5,
  "price": 155.00,
  "totalAmount": 775.00,
  "transactionDate": "2024-01-20T00:00:00Z",
  "commission": 5.00
}
```

**Errors:**
- `400` – Niepoprawne dane (np. sprzedaż więcej niż posiadasz)
- `404` – Portfel lub aktywo nie znalezione
- `409` – Nie wystarczająca ilość aktywów do sprzedaży

---

## Kody błędów

| Kod | Opis |
|-----|------|
| `200` | OK – Operacja powiodła się |
| `201` | Created – Zasób został utworzony |
| `204` | No Content – Operacja powiodła się, brak zawartości |
| `400` | Bad Request – Niepoprawne dane |
| `401` | Unauthorized – Brak tokenu lub token wygasł |
| `403` | Forbidden – Brak dostępu do zasobu |
| `404` | Not Found – Zasób nie znaleziony |
| `409` | Conflict – Konflikt (np. duplikat) |
| `500` | Internal Server Error – Błąd serwera |

---

## Autentykacja

Każdy request musi zawierać JWT token w nagłówku:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Token można uzyskać z Keycloak:

```bash
curl -X POST http://localhost:8080/realms/fin-insight/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=fin-insight&username=user&password=password"
```
