# 🤖 AI Advisor Service API

**Base URL:** `http://localhost:8083/api`  
**Auth:** JWT Bearer Token (wymagany)

---

## Rekomendacje

### POST /api/recommendations

Uzyskaj personalizowane rekomendacje inwestycyjne wspierane przez AI.

**Request:**
```bash
curl -X POST http://localhost:8083/api/recommendations \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "riskTolerance": "MEDIUM",
    "investmentAmount": 10000,
    "investmentHorizon": "5_YEARS",
    "preferences": ["STOCKS", "ETF"]
  }'
```

**Body:**
```json
{
  "userId": "uuid (required)",
  "riskTolerance": "LOW|MEDIUM|HIGH (required)",
  "investmentAmount": "number (required, > 0)",
  "investmentHorizon": "1_YEAR|3_YEARS|5_YEARS|10_YEARS (optional)",
  "preferences": "array of strings (optional)"
}
```

**Response (200):**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "riskTolerance": "MEDIUM",
  "investmentAmount": 10000,
  "investmentHorizon": "5_YEARS",
  "recommendations": [
    {
      "symbol": "SPY",
      "type": "ETF",
      "allocation": 40,
      "rationale": "Szeroki portfel akcji S&P 500, niskie koszty, dobre dla średniego ryzyka",
      "expectedReturn": 8.5,
      "riskLevel": "MEDIUM"
    },
    {
      "symbol": "BND",
      "type": "ETF",
      "allocation": 35,
      "rationale": "Obligacje korporacyjne, stabilność, dochód",
      "expectedReturn": 4.2,
      "riskLevel": "LOW"
    },
    {
      "symbol": "VGK",
      "type": "ETF",
      "allocation": 15,
      "rationale": "Akcje europejskie, dywersyfikacja geograficzna",
      "expectedReturn": 7.8,
      "riskLevel": "MEDIUM"
    },
    {
      "symbol": "GLD",
      "type": "ETF",
      "allocation": 10,
      "rationale": "Złoto, zabezpieczenie przed inflacją",
      "expectedReturn": 3.5,
      "riskLevel": "LOW"
    }
  ],
  "portfolioSummary": {
    "expectedAnnualReturn": 6.8,
    "expectedVolatility": 12.5,
    "sharpeRatio": 0.54,
    "diversificationScore": 0.85
  },
  "generatedAt": "2024-01-20T15:30:00Z"
}
```

---

## Parametry

### Risk Tolerance (Tolerancja ryzyka)

| Poziom | Opis | Akcje | Obligacje | Alternatywy |
|--------|------|-------|-----------|-------------|
| `LOW` | Konserwatywny | 20-30% | 60-70% | 10-20% |
| `MEDIUM` | Umiarkowany | 50-60% | 30-40% | 10-20% |
| `HIGH` | Agresywny | 70-80% | 10-20% | 10-20% |

### Investment Horizon (Horyzont inwestycyjny)

| Horyzont | Opis |
|----------|------|
| `1_YEAR` | Krótkoterminowy (do 1 roku) |
| `3_YEARS` | Średnioterminowy (1-3 lata) |
| `5_YEARS` | Średnioterminowy (3-5 lat) |
| `10_YEARS` | Długoterminowy (10+ lat) |

---

## Metryki portfela

### Expected Annual Return
Oczekiwany roczny zwrot z portfela (%).

```
= Σ(allocation_i × expected_return_i)
```

### Expected Volatility
Oczekiwana zmienność (ryzyko) portfela (%).

```
= √(Σ(allocation_i² × volatility_i²) + 2×Σ(correlation_ij × allocation_i × allocation_j × volatility_i × volatility_j))
```

### Sharpe Ratio
Miara zwrotu na jednostkę ryzyka.

```
= (Expected Return - Risk Free Rate) / Volatility
```

Wyższy Sharpe Ratio = lepszy stosunek zwrotu do ryzyka.

### Diversification Score
Ocena dywersyfikacji portfela (0-1).

```
0.0 = Brak dywersyfikacji (jeden instrument)
1.0 = Idealna dywersyfikacja
```

---

## Przykłady

### Konserwatywny inwestor

```bash
curl -X POST http://localhost:8083/api/recommendations \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "riskTolerance": "LOW",
    "investmentAmount": 50000,
    "investmentHorizon": "10_YEARS"
  }'
```

**Typowa alokacja:**
- 30% akcji (SPY, VTI)
- 60% obligacji (BND, AGG)
- 10% alternatyw (GLD)

---

### Agresywny inwestor

```bash
curl -X POST http://localhost:8083/api/recommendations \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "riskTolerance": "HIGH",
    "investmentAmount": 100000,
    "investmentHorizon": "5_YEARS",
    "preferences": ["STOCKS", "CRYPTO"]
  }'
```

**Typowa alokacja:**
- 70% akcji (QQQ, VGT, ARKK)
- 15% kryptowalut (BTC, ETH)
- 10% alternatyw (GLD, DBC)
- 5% obligacji (BND)

---

## Błędy

| Kod | Opis |
|-----|------|
| `200` | OK – Rekomendacje wygenerowane |
| `400` | Bad Request – Niepoprawne dane |
| `401` | Unauthorized – Brak tokenu |
| `404` | Not Found – Użytkownik nie znaleziony |
| `500` | Internal Server Error – Błąd AI |

---

## Limity

- **Maksymalnie 10 rekomendacji** na request
- **Maksymalnie 100 requestów** na godzinę
- **Timeout:** 30 sekund

---

## Caching

Rekomendacje są cachowane na **1 godzinę** dla tego samego użytkownika i parametrów.

```bash
# Sprawdzenie cache'u
redis-cli
> GET recommendations:550e8400-e29b-41d4-a716-446655440000:MEDIUM:10000
```

---

## Integracja z Portfolio Manager

Po uzyskaniu rekomendacji możesz je dodać do portfela:

```bash
# 1. Uzyskaj rekomendacje
curl -X POST http://localhost:8083/api/recommendations \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"userId": "...", "riskTolerance": "MEDIUM", ...}'

# 2. Dodaj aktywa do portfela
curl -X POST http://localhost:8081/api/portfolios/{portfolioId}/assets \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "SPY",
    "quantity": 40,
    "purchasePrice": 450.00,
    "purchaseDate": "2024-01-20"
  }'
```

---

## Model AI

Rekomendacje są generowane za pomocą:

- **Modern Portfolio Theory** – optymalizacja alokacji
- **Risk Assessment** – analiza tolerancji ryzyka
- **Market Data** – bieżące ceny i zmienność
- **Machine Learning** – personalizacja na podstawie historii

---

## Feedback

Aby ulepszyć rekomendacje, możesz wysłać feedback:

```bash
curl -X POST http://localhost:8083/api/recommendations/feedback \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "recommendationId": "...",
    "rating": 4,
    "comment": "Dobre rekomendacje, ale chciałbym więcej akcji"
  }'
```

(Endpoint planowany na przyszłość)
