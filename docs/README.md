# 📚 Fin-Insight Documentation

Kompletna dokumentacja projektu Fin-Insight. Wszystkie informacje potrzebne do pracy nad projektem.

## 📂 Struktura

```
docs/
├── README.md                 # Ten plik
├── architecture.md           # Architektura systemu
├── conventions.md            # Konwencje kodowania, branching, commity
├── llms.txt                  # Instrukcje dla agentów AI
└── api/
    ├── portfolios.md         # Portfolio Manager API
    ├── market-data.md        # Market Data Service API
    └── recommendations.md    # AI Advisor Service API
```

## 🚀 Szybki start

### 1. Przeczytaj architekturę
👉 [`architecture.md`](./architecture.md) – Zrozum jak system jest zbudowany

### 2. Naucz się konwencji
👉 [`conventions.md`](./conventions.md) – Jak pisać kod, commity, branche

### 3. Poznaj API
👉 [`api/`](./api/) – Szczegółowa dokumentacja endpointów

### 4. Dla agentów AI
👉 [`llms.txt`](./llms.txt) – Standardowe instrukcje dla AI

---

## 📖 Zawartość

### [`architecture.md`](./architecture.md)
- 🏗️ Diagram systemu
- 📋 Komponenty i ich role
- 🔄 Wzorce komunikacji
- 🔐 Bezpieczeństwo
- 📈 Skalowanie

### [`conventions.md`](./conventions.md)
- 🌿 Branchowanie (feature/ALF-XX/...)
- 💬 Commity (feat:, fix:, docs:, ...)
- 🔗 Integracja z Linear
- 🔄 Pull Request process
- ✅ Code review
- 📝 Dokumentacja kodu

### API Documentation

#### [`api/portfolios.md`](./api/portfolios.md)
- `GET /api/portfolios` – Pobierz portfele
- `POST /api/portfolios` – Utwórz portfel
- `PUT /api/portfolios/{id}` – Zaktualizuj portfel
- `DELETE /api/portfolios/{id}` – Usuń portfel
- `POST /api/portfolios/{id}/assets` – Dodaj aktywo
- `DELETE /api/portfolios/{id}/assets/{assetId}` – Usuń aktywo
- `GET /api/portfolios/{id}/transactions` – Historia transakcji
- `POST /api/portfolios/{id}/transactions` – Utwórz transakcję

#### [`api/market-data.md`](./api/market-data.md)
- `GET /api/symbols` – Wszystkie symbole
- `GET /api/symbols/{symbol}` – Symbol po kodzie
- `POST /api/symbols` – Dodaj symbol
- `GET /api/market-prices/latest` – Najnowsze ceny
- `GET /api/market-prices/symbol/{ticker}` – Historia cen
- `POST /api/market-prices` – Dodaj cenę

#### [`api/recommendations.md`](./api/recommendations.md)
- `POST /api/recommendations` – Uzyskaj rekomendacje AI
- Parametry: riskTolerance, investmentAmount, horizon
- Metryki: expectedReturn, volatility, sharpeRatio

### [`llms.txt`](./llms.txt)
Plik dla agentów AI zawierający:
- Przegląd projektu
- Stack technologiczny
- Konwencje branchowania i commitów
- Instrukcje Docker
- Linki do zasobów

---

## 🔍 Szukanie informacji

### Szukam informacji o...

| Temat | Plik |
|-------|------|
| Jak wygląda system? | [`architecture.md`](./architecture.md) |
| Jak pisać kod? | [`conventions.md`](./conventions.md) |
| Jak tworzyć branche? | [`conventions.md`](./conventions.md#-branchowanie) |
| Jak commitować? | [`conventions.md`](./conventions.md#-commity) |
| Jak linkować z Linear? | [`conventions.md`](./conventions.md#-integracja-z-linear) |
| Jak używać Portfolio API? | [`api/portfolios.md`](./api/portfolios.md) |
| Jak używać Market Data API? | [`api/market-data.md`](./api/market-data.md) |
| Jak używać AI API? | [`api/recommendations.md`](./api/recommendations.md) |
| Instrukcje dla AI? | [`llms.txt`](./llms.txt) |

---

## 🛠️ Uruchomienie

### Backend
```bash
cd backend
docker-compose up -d
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Dostęp
- Frontend: http://localhost:3000
- API: http://localhost:8081
- Keycloak: http://localhost:8080
- Swagger: http://localhost:8081/swagger-ui.html

---

## 📋 Checklist przed PR

- [ ] Branch: `feature/ALF-XX/opis`
- [ ] Commity: `feat:`, `fix:`, `docs:`, itd.
- [ ] Testy: `./gradlew test` (backend) / `npm test` (frontend)
- [ ] Dokumentacja: Zaktualizowana
- [ ] PR: Linkowany do Linear
- [ ] Review: Czeka na zatwierdzenie

---

## 🔗 Linki

- **GitHub**: https://github.com/AlfaTeam67/ZZPJ
- **Linear**: https://linear.app (Fin-Insight project)
- **Keycloak**: http://localhost:8080/admin
- **Eureka**: http://localhost:8761
- **Swagger**: http://localhost:8081/swagger-ui.html

---

## 💡 Wskazówki

### Dla programistów
1. Przeczytaj [`architecture.md`](./architecture.md) aby zrozumieć system
2. Przeczytaj [`conventions.md`](./conventions.md) aby znać standardy
3. Sprawdź odpowiedni plik w [`api/`](./api/) dla endpointów

### Dla agentów AI
1. Przeczytaj [`llms.txt`](./llms.txt) – zawiera wszystkie instrukcje
2. Postępuj zgodnie z konwencjami z [`conventions.md`](./conventions.md)
3. Sprawdzaj dokumentację API w [`api/`](./api/)

### Dla code review
1. Sprawdź konwencje w [`conventions.md`](./conventions.md)
2. Sprawdzaj API w [`api/`](./api/)
3. Upewnij się że branch i commity są poprawne

---

## 📞 Pytania?

- Sprawdź dokumentację w tym folderze
- Zapytaj w Linear
- Sprawdź GitHub issues

---

**Ostatnia aktualizacja:** 2025-01-20  
**Zespół:** AlfaTeam  
**Projekt:** ZZPJ 2025/2026
